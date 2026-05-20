package com.ecosystem.projectsservice.javaprojects.service.processes.files.file_removal;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.StructureSnapshot;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.CachedFileInvalidation;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.ProjectStructureInvalidation;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.read.HotLayerReader;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.update.HotLayerUpdater;
import com.ecosystem.projectsservice.javaprojects.service.storage.UserContentStorage;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.SimpleTriggerData;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.*;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.read.SnapshotService;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectActionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;


/*
политика:

удаление файла возможно, если никто из его родителей не удаляется в данный момент


сайд эффекты:
- Инвалидация структуры в начале
- Инвалидация структуры при компенсации (если произошло возвращение в available)
- Инвалидация кеша при удалении из storage
 */


@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_FILE_REMOVAL)
public class FileRemovalChain extends ControlledOutboxChain<FileRemovalEvent> {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private ProjectActionsUtils actionsUtils;


    @Autowired
    private UserContentStorage storage;

    @Autowired
    private FileRemovalChainCompensator compensator;


    @Autowired
    private HotLayerUpdater hotLayer;

    @Override
    protected ExternalEvent<? extends ExternalEventContext> bindResultingEvent() {
        return new ProjectEventFromUser();
    }



    @Override
    protected void setProcessAssociations(FileRemovalEvent event) {

    }

    @Override
    @Async("chainExecutor")
    @EventListener
    public void catchEvent(FileRemovalEvent event) {
        super.processEvent(event);
    }

    @OpeningStep(name = "polling")
    @Next(name = "prepareFile")
    public void checkAndPolling(FileRemovalEvent event){

        // данный запрос не блокирует файл, так как фаза опроса - долгая? либо же можно ввести статус polling для файла, но это кажется избыточным
        transaction().execute(status -> {
            Optional<File> initialCheck = fileRepository.findById(event.getExternalData().getFileId());
            if (initialCheck.isEmpty() || initialCheck.get().isHidden()) throw new IllegalStateException("файла не существует");
            event.getExternalData().setName(initialCheck.get().getName());
            event.getExternalData().setExtension(initialCheck.get().getExtension());
            return null;
        });





        event.setMessage("Запрос на удаление файла от: "+event.getContext().getUsername());


        Function<Map<String, TriggerAnswer>, Boolean> onFeedStrategy = (answers)->{
            System.out.println("зарегистрирован ответ для фазового триггера. Текущие ответы "+answers);

            for (TriggerAnswer answer:answers.values()){
                // демонстрация мгновенного отказа
                if (answer.isDecision()&& answer.getContent().equals("No")){
                    event.setMessage("отказ в удалении файла. Не получено одобрение других участников");
                    event.getInternalData().setCompensationPhase(true);
                    return true;
                }


            }
            return false;
        };

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
                    event.setMessage("отказ в удалении файла. Не получено одобрение других участников");
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
                .message(event.getContext().getUsername()+" собирается удалить файл "+event.getExternalData()
                        .getName()+"."+event.getExternalData().getExtension())
                .triggerExternalData(new SimpleTriggerData(TriggerType.YES_OR_NOT, Map.of("fileId",
                        event.getExternalData().getFileId().toString())))
                .build();


        // чтобы система увидела триггер, следующий шаг обязательно должен сопровождаться аннотацией waiting for
        createTrigger(phaseTrigger);





    }






    /*
    права на выполнение операции проверяются в верхнем уровне
     */


    @Step(name = "prepareFile")
    @WaitingFor(time = 20)
    @Next(name = "blockFile")
    public void prepareFile(FileRemovalEvent event){
        transaction().execute(status -> {
            Optional<File> fileBlock
                    = fileRepository.findByIdForUpdate(event.getExternalData().getFileId());
            if (fileBlock.isEmpty()){
                throw new IllegalStateException("файла не существует");
            }





            File file = fileBlock.get();

            if (file.getStatus()!=FileStatus.AVAILABLE)
                throw new IllegalStateException("файл сейчас недоступен");

            if (file.isHidden() || file.isImmutable()){
                throw new IllegalStateException("этот файл нельзя удалить");
            }

            file.setStatus(FileStatus.PREPARING_FOR_REMOVING);

            return null;




        });





    }

    @Step(name = "blockFile")
    @Next(name = "removeFileFromStorage")
    public void blockFile(FileRemovalEvent event){

       transaction().execute(status -> {
            Optional<File> fileBlock
                    = fileRepository.findByIdForUpdate(event.getExternalData().getFileId());
            if (fileBlock.isEmpty()){
                throw new IllegalStateException("файла больше не существует");
            }
            File file = fileBlock.get();

            if (file.getStatus()!=FileStatus.PREPARING_FOR_REMOVING)
                throw new IllegalStateException("неподходящий статус для шага block");


            // мы должны проверить, есть ли у родителей статус removing
           // одновременно проверяем, является ли родитель файла частью проекта

           boolean containsRoot = false;


           List<DirectoryReadOnly> directoriesAbove = snapshotService
                   .getParentsSnapshotDirectoriesOnly(file.getParent().getId());


           for (var dir:directoriesAbove){

               if (dir.getId().equals(event.getInternalData().getProjectRoot())){
                   containsRoot = true;
               }
               if (dir.getStatus()== DirectoryStatus.REMOVING
                       || dir.getStatus() == DirectoryStatus.PREPARING_FOR_REMOVAL){
                   throw new IllegalStateException("родительские директории заняты другим процессом");
               }
           }




           if (!containsRoot) throw new IllegalStateException("файл не принадлежит проекту");







           // проставляем окончательный блокирующий статус
           file.setStatus(FileStatus.REMOVING);




            return null;
        });




       try {
           // инвалидируем кеш файла и кеш структуры
           hotLayer.fileInvalidation(new CachedFileInvalidation(event.getExternalData().getFileId()));
           // - в статусе кандидата на удаления он исчезнет из предложек
           hotLayer.projectStructureInvalidation(
                   new ProjectStructureInvalidation(event.getContext().getProjectId())
           );
       }

       catch (Exception e){
           e.printStackTrace();
       }















    }



    @Step(name = "removeFileFromStorage")
    @Next(name = "removeFileFromDb")
    @MaxRetry(maxCount = 3)
    public void removeFileFromStorage(FileRemovalEvent event){


        storage.delete(event.getExternalData().getFileId().toString());


    }

    @EndingStep(name = "removeFileFromDb")
    public void removeFileFromDb(FileRemovalEvent event){



        transaction().execute(status -> {

            fileRepository.deleteById(event.getExternalData().getFileId());

            return null;
        });


    }





    @Override
    public void compensationStrategy(FileRemovalEvent event) {
        compensator.compensation(event);
    }
}
