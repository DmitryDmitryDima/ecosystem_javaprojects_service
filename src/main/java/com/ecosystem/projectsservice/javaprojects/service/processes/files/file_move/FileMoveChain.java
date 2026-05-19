package com.ecosystem.projectsservice.javaprojects.service.processes.files.file_move;

import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.CodeService;
import com.ecosystem.projectsservice.javaprojects.transport.broadcast.Broadcast;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.read.SnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;


// TODO инвалидация кеша
@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_FILE_MOVE)
public class FileMoveChain extends ControlledOutboxChain<FileMoveEvent> {




    @Autowired
    private FileMoveChainCompensator compensator;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private SnapshotService snapshotService;

    @Override
    protected ExternalEvent<? extends ExternalEventContext> bindResultingEvent() {
        return new ProjectEventFromUser();
    }

    @Override
    protected void setProcessAssociations(FileMoveEvent event) {

    }

    @Override
    @Async("chainExecutor")
    @EventListener
    public void catchEvent(FileMoveEvent event) {
        super.processEvent(event);
    }


    // todo тут нужен механизм отслеживания version - мы должны гарантировать, что мы компенсируем наш же процесс

    // todo для цепей подобной сложности может понадобится система enum или даже отдельный error объект для фиксации конкретной причины ошибки
    @Override
    public void compensationStrategy(FileMoveEvent event) {
        compensator.compensation(event);
    }




    // по идее мы должны поставить статус prepare for generating на директорию и preparing_for_migrating на файл

    @OpeningStep(name = "preparing")
    @Message
    @Next(name = "block_entities")
    public void preparing(FileMoveEvent fileMoveEvent){

        fileMoveEvent.setMessage("Выполняем подготовку сущностей");
        transaction().execute(status -> {

            Optional<File> fileCheck = fileRepository
                    .findByIdForUpdate(fileMoveEvent.getExternalData().getFileId());

            if (fileCheck.isEmpty())
                throw new IllegalStateException("файла больше не существует");
            if (fileCheck.get().getStatus()!=FileStatus.AVAILABLE)
                throw new IllegalStateException("Неподходящий статус файла на стадии preparing");
            if (fileCheck.get().isHidden() || fileCheck.get().isImmutable())
                throw new IllegalStateException("Файл не может быть перемещен");


            Optional<Directory> directoryCheck
                    = directoryRepository.findByIdForUpdate(fileMoveEvent.getExternalData().getParent());

            if (directoryCheck.isEmpty())
                throw new IllegalStateException("директории больше не существует");
            if (directoryCheck.get().getStatus()!=DirectoryStatus.AVAILABLE)
                throw new IllegalStateException("неподходящий статус директории на стадии preparing");

            // СТАВИМ СТАТУСЫ, ЗАЩИЩАЮЩИЕ ОТ ДРУГИМ ПРОЦЕССОВ. ЕСЛИ ДАННЫЕ СТАТУСЫ НЕ СОХРАНЯЮТСЯ В СЛЕДУЮЩЕМ ШАГЕ, ЭТО ОЗНАЧАЕТ, ЧТО ПРОЦЕСС НАРУШЕН
            directoryCheck.get().setStatus(DirectoryStatus.PREPARING_FOR_GENERATING);
            fileCheck.get().setStatus(FileStatus.PREPARING_FOR_MIGRATING);


            return null;
        });
    }

    @Step(name = "block_entities")
    @Message
    @Next(name = "db_parent_switch")
    // todo добавить проверку на принадлежность файла и его нового родителя проекту
    public void blockEntities(FileMoveEvent event){
        event.setMessage("блокируем сущности");

        transaction().execute(status -> {

            Optional<File> fileCheck
                    = fileRepository.findByIdForUpdate(event.getExternalData().getFileId());

            if (fileCheck.isEmpty())
                throw new IllegalStateException("файла больше не существует");
            if (fileCheck.get().getStatus()!=FileStatus.PREPARING_FOR_MIGRATING)
                throw new IllegalStateException("Неподходящий статус файла на стадии block");

            Optional<Directory> directoryCheck
                    = directoryRepository.findByIdForUpdate(event.getExternalData().getParent());

            if (directoryCheck.isEmpty())
                throw new IllegalStateException("директории больше не существует");
            if (directoryCheck.get().getStatus()!=DirectoryStatus.PREPARING_FOR_GENERATING)
                throw new IllegalStateException("неподходящий статус директории на стадии block");


            // политика анализа снимков.
            // У файла мы должны проверить все директории его родителя
            // - нет ли среди них, кто собирается мигрировать или удаляться
            List<DirectoryReadOnly> fileParents
                    = snapshotService.getParentsSnapshotDirectoriesOnly(fileCheck.get().getParent().getId());

            boolean containsRoot = false;
            boolean containsDirectory = false;

            for (DirectoryReadOnly directoryReadOnly:fileParents){
                if (directoryReadOnly.getId().equals(event.getInternalData().getProjectRoot())){
                    containsRoot = true;
                }

                if (directoryReadOnly.getId().equals(fileCheck.get().getParent().getId())){
                    containsDirectory = true;
                }

                if (directoryReadOnly.getStatus()==DirectoryStatus.REMOVING

                        || directoryReadOnly.getStatus() == DirectoryStatus.PREPARING_FOR_REMOVAL
                        ){

                    throw new IllegalStateException("Кто то из родителей занят другим процессом");
                }
            }

            if (!(containsDirectory && containsRoot)){
                throw new IllegalStateException("Файл не принадлежит проекту");
            }



            // у папки, в которую мы собираемся перемещать,
            // мы должны проверить родителей на migrating и removing. Среди детей не должно быть одноименных
            List<DirectoryReadOnly> parentParents
                    = snapshotService.getParentsSnapshotDirectoriesOnly(directoryCheck.get().getId());

            containsDirectory = false;
            containsRoot = false;

            for (DirectoryReadOnly directoryReadOnly:parentParents){
                if (directoryReadOnly.getId().equals(directoryCheck.get().getId())){
                    containsDirectory = true;

                }

                if (directoryReadOnly.getId().equals(event.getInternalData().getProjectRoot())){
                    containsRoot = true;
                }

                if (directoryReadOnly.getStatus()==DirectoryStatus.REMOVING
                        || directoryReadOnly.getStatus() == DirectoryStatus.PREPARING_FOR_REMOVAL){
                    throw new IllegalStateException("Папка для перемещения или" +
                            " ее родитель заблокированы сторонним процессом");
                }






            }
            if (!(containsDirectory && containsRoot))
                throw new IllegalStateException("Папка, в которую перемещают файл, не относится к проекту");





            List<FileReadOnly> parentFiles
                    = snapshotService.getFilesForDirectory(directoryCheck.get().getId());
            if (parentFiles.stream()
                    .anyMatch(file->file.getName().equals(fileCheck.get().getName())
                            && file.getExtension().equals(fileCheck.get().getExtension()))){

                throw new IllegalStateException("Файл с именем перемещаемого файла уже существует");
            }


            fileCheck.get().setStatus(FileStatus.MIGRATING);

            directoryCheck.get().setStatus(DirectoryStatus.GENERATING);


            event.getExternalData().setExtension(fileCheck.get().getExtension());
            event.getExternalData().setFilename(fileCheck.get().getName());



            return null;
        });
    }

    // мы меняем родителя у файла
    @Step(name = "db_parent_switch")
    @Message
    @Next(name="release")
    public void parentSwitch(FileMoveEvent event){
        event.setMessage("Перестраиваем базу данных");
        transaction().execute(status -> {

            Optional<File> fileCheck = fileRepository.findByIdForUpdate(event.getExternalData().getFileId());

            if (fileCheck.isEmpty()) throw new IllegalStateException("файла больше не существует");

            File file = fileCheck.get();
            if (file.getStatus()!=FileStatus.MIGRATING)
                throw new IllegalStateException("Неподходящий статус файла на стадии parent switch");

            Optional<Directory> directoryCheck = directoryRepository
                    .findByIdForUpdate(event.getExternalData().getParent());

            if (directoryCheck.isEmpty())
                throw new IllegalStateException("директории больше не существует");
            Directory directory = directoryCheck.get();
            if (directory.getStatus()!=DirectoryStatus.GENERATING)
                throw new IllegalStateException("неподходящий статус директории на стадии parent switch");


            file.setParent(directory);
            directory.getFiles().add(file);

            // не забываем переписать путь у файла
            file
                    .setConstructedPath(Path
                            .of(directory.getConstructedPath(), file.getName()+"."+file.getExtension())
                    .normalize().toString());






            return null;
        });
    }


    // сброс статусов
    @EndingStep(name = "release")
    public void release(FileMoveEvent event){
        transaction().execute(status -> {

            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParent());

            if (directoryCheck.isEmpty())
                throw new IllegalStateException("директории больше не существует");

            Directory directory = directoryCheck.get();

            Optional<File> newChild = directory.getFiles().stream().filter(file -> file.getId().equals(event.getExternalData().getFileId())).findFirst();
            if (newChild.isEmpty())
                throw new IllegalStateException("Ошибка перемещения на этапе release. Состояние бд не было обновлено");

            newChild.get().setStatus(FileStatus.AVAILABLE);
            directory.setStatus(DirectoryStatus.AVAILABLE);



            return null;
        });

        try {
            //cacheOperations(event);
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }

    /*
    private void cacheOperations(FileMoveEvent event) throws Exception{
        FileDTO dto;

        Optional<FileDTO> cacheCheck = fileCache.read(event.getExternalData().getFileId());

        dto = cacheCheck.orElseGet(() -> {
            try {
                return FileDTO.builder()
                        .id(event.getExternalData().getFileId())
                        .extension(event.getExternalData().getExtension())
                        .name(event.getExternalData().getFilename())
                        .projectId(event.getContext().getProjectId())
                        .ownerUUID(event.getInternalData().getProjectOwner())
                        .content(ProjectUtils.readFile(Path.of(event.getInternalData().getProjectsPath(),
                                event.getExternalData().getConstructedPath())))
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        dto.setConstructedPath(event.getExternalData().getConstructedPath());

        String[] way = dto.getConstructedPath().split("\\\\");

        System.out.println(Arrays.toString(way)+" way");



        boolean isJavaDirectoryVisited = false;


        StringBuilder newPackageName = new StringBuilder();

        for (int x = 0; x< way.length-1; x++){
            String component = way[x];

            if (isJavaDirectoryVisited){
                newPackageName.append(component);
                if (x< way.length-2){
                    newPackageName.append(".");
                }


            }



            if (component.equals("java")){
                isJavaDirectoryVisited = true;
            }
        }


        System.out.println(newPackageName);


        dto.setContent(codeService.transformPackage(dto.getContent(), newPackageName.toString()));





        fileCache.save(event.getExternalData().getFileId(), dto);
        // todo как быть с гонкой при доступе к dto объекту кеша?
        broadcast.sendAsync(new Broadcast.EventBuilder().useEvent(ProjectEventFromUser::new)
                .withContext(event::getContext).withData(()->{
                    FileSaveExternalData data = new FileSaveExternalData();
                    data.setFileId(dto.getId());
                    data.setFileOwner(event.getInternalData().getProjectOwner());
                    data.setPath(dto.getConstructedPath());
                    data.setExtension(dto.getExtension());
                    data.setName(dto.getName());
                    data.setContent(dto.getContent());
                    return data;}
                ).withType(ExternalEventType.JAVA_PROJECT_FILE_SAVE)
                .withMessage("файл сохранен").build());

    }

     */


}
