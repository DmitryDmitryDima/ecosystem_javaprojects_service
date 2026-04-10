package com.ecosystem.projectsservice.javaprojects.service.processes.files.file_add;

import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.CodeService;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.SnapshotService;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectActionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_FILE_ADD)
public class FileAddChain extends ControlledOutboxChain<FileAddEvent> {


    @Autowired
    private CodeService codeService;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private ProjectActionsUtils actionsUtils;

    @Override
    protected ExternalEvent<? extends ExternalEventContext> bindResultingEvent() {
        return new ProjectEventFromUser();
    }

    @Override
    protected void setProcessAssociations(FileAddEvent event) {

    }

    @Override
    @Async("chainExecutor")
    @EventListener
    public void catchEvent(FileAddEvent event) {
       super.processEvent(event);
    }

    @Override
    public void compensationStrategy(FileAddEvent event) {
        if (!event.getInternalData().getCurrentStep().equals("block_directory")){
            transaction().execute(status -> {

                Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParentId());
                if (directoryCheck.isEmpty()) throw new IllegalStateException("директории не существует");
                directoryCheck.get().setStatus(DirectoryStatus.AVAILABLE);

                return null;
            });
            if (event.getExternalData().getId()!=null){
                // удаляем созданную сущность, если она есть
                transaction().execute(status -> {
                    fileRepository.deleteById(event.getExternalData().getId());
                    return null;
                });
            }



        }
    }

    @OpeningStep(name = "prepare_directory")
    @Next(name = "block_directory")
    @Message
    public void prepareDirectory(FileAddEvent fileAddEvent){
        fileAddEvent.setMessage("резервируем родительскую директорию");
        transaction().execute(status -> {

            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(fileAddEvent.getExternalData().getParentId());
            if (directoryCheck.isEmpty() || directoryCheck.get().isHidden())
                throw new IllegalStateException("директории не существует");
            if (directoryCheck.get().getStatus()!=DirectoryStatus.AVAILABLE)
                throw new IllegalStateException("директория занята другим процессом");

            directoryCheck.get().setStatus(DirectoryStatus.PREPARING_FOR_GENERATING);


            return null;
        });
    }

    // директория блокируется на операции удаления и перемещения - статус generating
    @Step(name = "block_directory")
    @Next(name="create_db_entity")
    @Message
    public void blockDirectory(FileAddEvent fileAddEvent){
        fileAddEvent.setMessage("Проверяем директорию");

        transaction().execute(status -> {

            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(fileAddEvent.getExternalData().getParentId());
            if (directoryCheck.isEmpty()) throw new IllegalStateException("директории не существует");

            Directory directory = directoryCheck.get();



            /*
            Анализируем снимок верхних уровней иерархии - мы должны проверить, есть ли в ответе одновременно parent id и project root
            Если нет - это означает, что директория не принадлежит проекту
            Помимо этого - проверяем статусы на самой директории и на родителях. Статус Removing или migrating - автоматически отмена
            проверяем также список существующих папок и файлов
            */

            List<DirectoryReadOnly> parents = snapshotService.getParentsSnapshotDirectoriesOnly(fileAddEvent.getExternalData().getParentId());

            System.out.println(parents);


            boolean parentContains = false;
            boolean rootContains = false;

            for (DirectoryReadOnly directoryReadOnly:parents){


                if (directoryReadOnly.getId().equals(fileAddEvent.getExternalData().getParentId())){
                    parentContains = true;
                }

                else {

                    if (directoryReadOnly.getStatus()==DirectoryStatus.REMOVING || directoryReadOnly.getStatus() == DirectoryStatus.MIGRATING
                    || directoryReadOnly.getStatus()==DirectoryStatus.PREPARING_FOR_REMOVAL
                            || directoryReadOnly.getStatus()==DirectoryStatus.PREPARING_FOR_MIGRATING
                    ){
                        throw new IllegalStateException("используемая папка заблокирована другим процессом");
                    }

                    // root
                    if (directoryReadOnly.getId().equals(fileAddEvent.getInternalData().getProjectRoot())){
                        rootContains = true;
                    }
                }







            }

            if (!(parentContains && rootContains)){
                throw new IllegalStateException("директория не принадлежит проекту");
            }

            List<FileReadOnly> files = snapshotService.getFilesForDirectory(fileAddEvent.getExternalData().getParentId());

            if (files.stream().anyMatch(fileReadOnly -> fileReadOnly.getExtension().equals(fileAddEvent.getExternalData().getExtension())
                            && fileReadOnly.getName().equals(fileAddEvent.getExternalData().getFilename())

                    )){
                throw new IllegalStateException("файл с таким именем уже есть");
            };




            // данный статус блокирует операцию удаления и операцию перемещения
            directory.setStatus(DirectoryStatus.GENERATING);

            return null;
        });
    }

    @Step(name="create_db_entity")
    @Next(name = "write_file_to_disk")
    @Message
    public void createDbEntity(FileAddEvent event){
        event.setMessage("Создаем файл");
        File created = transaction().execute(status -> {

            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParentId());

            if (directoryCheck.isEmpty()) throw new IllegalStateException("директории не существует");
            Directory directory = directoryCheck.get();

            File file = new File();
            file.setStatus(FileStatus.AVAILABLE);
            file.setName(event.getExternalData().getFilename());
            file.setExtension(event.getExternalData().getExtension());
            file.setCreatedAt(Instant.now());
            file.setUpdatedAt(Instant.now());
            file.setConstructedPath(Path.of(directory.getConstructedPath(),
                    event.getExternalData().getFilename()+"."+event.getExternalData().getExtension()).normalize().toString());

            directory.getFiles().add(file);
            file.setParent(directory);



            return file;
        }
        );

        System.out.println(created.getId()+" created");

        event.getInternalData()
                .setFilepath(Path.of(event.getInternalData().getProjectsPath(),
                        created.getConstructedPath()).normalize().toString());
        event.getExternalData().setId(created.getId());
        event.getExternalData().setConstructedPath(created.getConstructedPath());
    }

    @Step(name = "write_file_to_disk")
    @Next(name = "release_directory")
    @MaxRetry(maxCount = 3)
    public void writeFileToDisk(FileAddEvent event){
        String initialContent = resolveInitialContent(event);




        try {
            Files.writeString(Path.of(event.getInternalData().getFilepath()), initialContent, StandardOpenOption.CREATE_NEW);
            //Files.createFile(Path.of(event.getInternalData().getFilepath()));
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка записи файла в диск "+e.getMessage());
        }


    }


    private String resolveInitialContent(FileAddEvent event){

        // проверяем расширение
        if (!event.getExternalData().getExtension().equals("java")) return "";
        Path normalizedJavaChunk = Path.of("/src/main/java/").normalize();


        // проверяем, находится ли java файл в java директории
        if (!Path.of(event.getExternalData().getConstructedPath()).normalize().toString()
                .contains(normalizedJavaChunk.toString())) return "";

        try {
            String[] way = event.getExternalData().getConstructedPath().split("\\\\");
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

            return codeService.createEmptyPublicClass(newPackageName.toString(), event.getExternalData().getFilename());


        }
        catch (Exception e){
            return "";
        }






    }


    @EndingStep(name = "release_directory")
    public void release(FileAddEvent event){

        transaction().execute(status -> {

            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParentId());
            if (directoryCheck.isEmpty()) throw new IllegalStateException("директории не существует");
            directoryCheck.get().setStatus(DirectoryStatus.AVAILABLE);

            return null;
        });

    }



}
