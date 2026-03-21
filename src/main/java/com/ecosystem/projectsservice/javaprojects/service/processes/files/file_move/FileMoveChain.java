package com.ecosystem.projectsservice.javaprojects.service.processes.files.file_move;

import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.triggers.SimpleTriggerData;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.PhaseStrategy;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.PhaseTrigger;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.TriggerAnswer;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.TriggerType;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.SnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_FILE_MOVE)
public class FileMoveChain extends ControlledOutboxChain<FileMoveEvent> {

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
        if (event.getInternalData().getCurrentStep().equals("preparing") || event.getInternalData().getCurrentStep().equals("block_entities") ||
        event.getInternalData().getCurrentStep().equals("db_parent_switch")
        ){
            transaction().execute(status -> {

                Optional<File> fileCheck = fileRepository.findById(event.getExternalData().getFileId());

                if (fileCheck.isEmpty()) throw new IllegalStateException("Файла не существует");

                fileCheck.get().setStatus(FileStatus.AVAILABLE);

                Optional<Directory> directory =directoryRepository.findById(event.getExternalData().getParent());
                if (directory.isEmpty()) throw new IllegalStateException("Директории не существует");

                directory.get().setStatus(DirectoryStatus.AVAILABLE);



                return null;
            });
        };
    }


    /*
    @OpeningStep(name="polling")
    @Next(name="preparing")
    public void polling(FileMoveEvent event){


        class MoveInitialCheck {
            final File file; final Directory directory;
            MoveInitialCheck(File f, Directory d){
                this.file = f;
                this.directory = d;
            }
        }

        MoveInitialCheck check = transaction().execute(status -> {
            Optional<File> fileCheck = fileRepository.findById(event.getExternalData().getFileId());

            if (fileCheck.isEmpty()) throw new IllegalStateException("Файла не существует");
            if (fileCheck.get().isHidden() || fileCheck.get().isImmutable()) throw new IllegalStateException("Файл не может быть перемещен");

            if (fileCheck.get().getStatus()!= FileStatus.AVAILABLE) throw new IllegalStateException("Файл занят другим процессом");

            // ситуация, в которой перемещение происходит в папку. которая уже является родителем
            if (fileCheck.get().getParent().getId().equals(event.getExternalData().getParent())){
                throw new IllegalStateException("Директория - прямой родитель");
            }

            Optional<Directory> directory =directoryRepository.findById(event.getExternalData().getParent());
            if (directory.isEmpty()) throw new IllegalStateException("Директории, куда вы собираетесь перемещать, не существует");
            if (directory.get().isHidden()) throw new IllegalStateException("Операция запрещена для данной директории");
            if (directory.get().getStatus()!= DirectoryStatus.AVAILABLE) throw new IllegalStateException("директория занята другим процессом");

            return new MoveInitialCheck(fileCheck.get(), directory.get());
        });

        event.setMessage("Запрос на перемещения файла "+
                check.file.getConstructedPath()+"/"+check.file.getName()+" в директорию "+check.directory.getConstructedPath()
                +"/"+check.directory.getName());

        event.getExternalData().setFilename(check.file.getName());
        event.getExternalData().setExtension(check.file.getExtension());
        event.getExternalData().setDirectoryName(check.directory.getName());

        Function<Map<String, TriggerAnswer>, Boolean> onFeedStrategy = (answers -> {
            for (TriggerAnswer answer:answers.values()){
                // демонстрация мгновенного отказа
                if (answer.isDecision()&& answer.getContent().equals("No")){
                    event.setMessage("отказ в перемещении файла. Не получено одобрение других участников, просматривающих файл");
                    event.getInternalData().setCompensationPhase(true);
                    return true;
                }
            }
            return false;

        });

        Function<Map<String, TriggerAnswer>, Boolean> activityPollingPhaseStrategy = (answers)->{

            System.out.println("activity check phase");
            for (TriggerAnswer answer:answers.values()){
                // если обнаружен кто то, кто не принял решение. ждем его
                if (!answer.isDecision()){
                    return false;
                }
            }
            // если все согласны, то очередь выполняет следующий шаг
            return true;
        };

        // конечная фаза - тут необходимо принять решение о том, продолжать ли цепочку
        Function<Map<String, TriggerAnswer>, Boolean> finalDecisionPhaseStrategy = (answers)->{

            System.out.println("final decision phase");

            for (TriggerAnswer answer:answers.values()){
                if (!answer.isDecision()){
                    event.setMessage("отказ в перемещении - не дождались сигнала от других участников");
                    event.getInternalData().setCompensationPhase(true);
                    return false;
                }
            }


            return true;
        };

        PhaseStrategy strategy = PhaseStrategy.constructStrategy()
                .addPhase(activityPollingPhaseStrategy, 500)
                .addPhase(finalDecisionPhaseStrategy, 5000)
                .getStrategy();

        PhaseTrigger phaseTrigger = PhaseTrigger.builder()
                .phaseStrategy(strategy)
                .onFeedStrategy(onFeedStrategy)
                .needPollingMessage(true)
                .correlationId(event.getContext().getCorrelationId())
                .message(event.getMessage())
                .triggerExternalData(new SimpleTriggerData(TriggerType.YES_OR_NOT, Map.of("directoryId",
                        event.getExternalData().getParent().toString(),"fileId", event.getExternalData().getFileId().toString())))
                .build();


        createTrigger(phaseTrigger);


    }

     */

    // по идее мы должны поставить статус prepare for generating на директорию и preparing_for_migrating на файл

    @OpeningStep(name = "preparing")
    //@WaitingFor(time = 20)
    @Message
    @Next(name = "block_entities")
    public void preparing(FileMoveEvent fileMoveEvent){

        fileMoveEvent.setMessage("Выполняем подготовку сущностей");
        transaction().execute(status -> {

            Optional<File> fileCheck = fileRepository.findByIdForUpdate(fileMoveEvent.getExternalData().getFileId());

            if (fileCheck.isEmpty()) throw new IllegalStateException("файла больше не существует");
            if (fileCheck.get().getStatus()!=FileStatus.AVAILABLE) throw new IllegalStateException("Неподходящий статус файла на стадии preparing");
            if (fileCheck.get().isHidden() || fileCheck.get().isImmutable()) throw new IllegalStateException("Файл не может быть перемещен");


            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(fileMoveEvent.getExternalData().getParent());

            if (directoryCheck.isEmpty()) throw new IllegalStateException("директории больше не существует");
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

            Optional<File> fileCheck = fileRepository.findByIdForUpdate(event.getExternalData().getFileId());

            if (fileCheck.isEmpty()) throw new IllegalStateException("файла больше не существует");
            if (fileCheck.get().getStatus()!=FileStatus.PREPARING_FOR_MIGRATING) throw new IllegalStateException("Неподходящий статус файла на стадии block");

            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParent());

            if (directoryCheck.isEmpty()) throw new IllegalStateException("директории больше не существует");
            if (directoryCheck.get().getStatus()!=DirectoryStatus.PREPARING_FOR_GENERATING)
                throw new IllegalStateException("неподходящий статус директории на стадии blocking");


            // политика анализа снимков.
            // У файла мы должны проверить все директории его родителя - нет ли среди них, кто собирается мигрировать или удаляться
            List<DirectoryReadOnly> fileParents = snapshotService.getParentsSnapshotDirectoriesOnly(fileCheck.get().getParent().getId());

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
                        || directoryReadOnly.getStatus() == DirectoryStatus.MIGRATING
                        || directoryReadOnly.getStatus() == DirectoryStatus.PREPARING_FOR_REMOVAL
                        || directoryReadOnly.getStatus() == DirectoryStatus.PREPARING_FOR_MIGRATING){

                    throw new IllegalStateException("Кто то из родителей занят другим процессом");
                }
            }

            if (!(containsDirectory && containsRoot)){
                throw new IllegalStateException("Файл не принадлежит проекту");
            }



            // у папки, в которую мы собираемся перемещать, мы должны проверить родителей на migrating и removing. Среди детей не должно быть одноименных
            List<DirectoryReadOnly> parentParents = snapshotService.getParentsSnapshotDirectoriesOnly(directoryCheck.get().getId());

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
                        || directoryReadOnly.getStatus() == DirectoryStatus.MIGRATING
                        || directoryReadOnly.getStatus() == DirectoryStatus.PREPARING_FOR_REMOVAL
                        || directoryReadOnly.getStatus() == DirectoryStatus.PREPARING_FOR_MIGRATING){
                    throw new IllegalStateException("Папка для перемещения или ее родитель заблокированы сторонним процессом");
                }






            }
            if (!(containsDirectory && containsRoot)) throw new IllegalStateException("Папка, в которую вы кидаете, не в проекте");





            List<FileReadOnly> parentFiles = snapshotService.getFilesForDirectory(directoryCheck.get().getId());
            if (parentFiles.stream()
                    .anyMatch(file->file.getName().equals(fileCheck.get().getName())
                            && file.getExtension().equals(fileCheck.get().getExtension()))){

                throw new IllegalStateException("Файл с именем перемещаемого файла уже существует");
            }


            fileCheck.get().setStatus(FileStatus.MIGRATING);

            directoryCheck.get().setStatus(DirectoryStatus.GENERATING);



            return null;
        });
    }

    // мы меняем родителя у файла
    @Step(name = "db_parent_switch")
    @Message
    @Next(name="disk_transfer")
    public void parentSwitch(FileMoveEvent event){
        event.setMessage("Перестраиваем базу данных");
        transaction().execute(status -> {

            Optional<File> fileCheck = fileRepository.findByIdForUpdate(event.getExternalData().getFileId());

            if (fileCheck.isEmpty()) throw new IllegalStateException("файла больше не существует");

            File file = fileCheck.get();
            if (file.getStatus()!=FileStatus.MIGRATING) throw new IllegalStateException("Неподходящий статус файла на стадии parent switch");

            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParent());

            if (directoryCheck.isEmpty()) throw new IllegalStateException("директории больше не существует");
            Directory directory = directoryCheck.get();
            if (directory.getStatus()!=DirectoryStatus.GENERATING)
                throw new IllegalStateException("неподходящий статус директории на стадии parent switch");


            file.setParent(directory);
            directory.getFiles().add(file);

            // не забываем переписать путь у файла
            event.getInternalData().setOldPath(file.getConstructedPath());
            file.setConstructedPath(Path.of(directory.getConstructedPath(), file.getName()+"."+file.getExtension())
                    .normalize().toString());





            return null;
        });
    }

    @Step(name="disk_transfer")
    @Message
    @Next(name = "release")
    public void diskTransfer(FileMoveEvent event){

        event.setMessage("согласуем с диском");
        // тут необходима проверка, что связь действительно существует в бд
        File transferedFile = transaction().execute(status -> {

            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParent());

            if (directoryCheck.isEmpty()) throw new IllegalStateException("директории больше не существует");
            Directory directory = directoryCheck.get();

            Optional<File> newChild = directory.getFiles().stream().filter(file -> file.getId().equals(event.getExternalData().getFileId())).findFirst();
            if (newChild.isEmpty()) throw new IllegalStateException("Ошибка перемещения. Состояние бд не было обновлено");


            return newChild.get();
        });

        // сейчас файл физически хранится тут
        Path oldPath = Path.of(event.getInternalData().getProjectsPath(), event.getInternalData().getOldPath());
        // его нужно переместить сюда
        Path newPath = Path.of(event.getInternalData().getProjectsPath(), transferedFile.getConstructedPath());

        try {
            Files.move(oldPath, newPath);
        }
        catch (Exception e){
            throw new IllegalStateException("Ошибка перемещения файла на диске "+e.getMessage());
        }


    }
    // сброс статусов
    @EndingStep(name = "release")
    public void release(FileMoveEvent event){
        transaction().execute(status -> {

            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParent());

            if (directoryCheck.isEmpty()) throw new IllegalStateException("директории больше не существует");

            Directory directory = directoryCheck.get();

            Optional<File> newChild = directory.getFiles().stream().filter(file -> file.getId().equals(event.getExternalData().getFileId())).findFirst();
            if (newChild.isEmpty()) throw new IllegalStateException("Ошибка перемещения на этапе release. Состояние бд не было обновлено");

            newChild.get().setStatus(FileStatus.AVAILABLE);
            directory.setStatus(DirectoryStatus.AVAILABLE);



            return null;
        });
    }


}
