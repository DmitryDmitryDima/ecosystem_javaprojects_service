package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_removal;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.StructureSnapshot;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.DirectoryRemoval;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.ProjectStructureInvalidation;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.update.HotLayerUpdater;
import com.ecosystem.projectsservice.javaprojects.service.storage.UserContentStorage;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.SimpleTriggerData;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.PhaseStrategy;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.PhaseTrigger;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.TriggerAnswer;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.TriggerType;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.read.SnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

// todo polling фаза в данном случае требует более сложной обработки на фронтенде
// фронтенд должен понять - сидит ли пользователь в каком либо из детей удаляемой директории
// в теории мы можем сформировать и послать список файлов, которые можно считать удаляемыми

/*
сайд эффекты - инвалидация структуры, инвалидация файлового кеша, в будущем - удаление ссылок
 */
@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_REMOVE_DIRECTORY)
public class DirectoryRemovalChain extends ControlledOutboxChain<DirectoryRemovalEvent> {



    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private UserContentStorage storage;

    @Autowired
    private DirectoryRemovalChainCompensator compensator;


    @Autowired
    private HotLayerUpdater hotLayer;

    @Override
    protected ExternalEvent<? extends ExternalEventContext> bindResultingEvent() {
        return new ProjectEventFromUser();
    }

    @Override
    protected void setProcessAssociations(DirectoryRemovalEvent event) {

    }

    @Override
    @Async("chainExecutor")
    @EventListener
    public void catchEvent(DirectoryRemovalEvent event) {
        super.processEvent(event);
    }

    @Override
    public void compensationStrategy(DirectoryRemovalEvent event) {


        compensator.compensation(event);

    }


    @OpeningStep(name = "polling")
    @Next(name = "prepare_directory")
    public void polling(DirectoryRemovalEvent event){


        // проверка доступности директории на момент опроса
        Directory directory = transaction().execute(status -> {
            Optional<Directory> initialCheck
                    = directoryRepository.findById(event.getExternalData().getId());
            if (initialCheck.isEmpty() || initialCheck.get().isHidden())
                throw new IllegalStateException("директории не существует");


            if (initialCheck.get().isImmutable())
                throw new IllegalStateException("Эту директорию нельзя удалить");
            if (initialCheck.get().getStatus()!=DirectoryStatus.AVAILABLE)
                throw new IllegalStateException("Директория занята другим процессом");
            event.getExternalData().setName(initialCheck.get().getName());
           return null;
        });

        // обновление сообщения для ивента цепочки
        event.setMessage("Запрос на удаление директории от "+event.getContext().getUsername());

        // стратегия обработки каждого из ответов
        Function<Map<String, TriggerAnswer>, Boolean> onFeedStrategy = (answers)->{


            System.out.println("on answer callback "+answers);
            for (TriggerAnswer answer:answers.values()){
                // демонстрация мгновенного отказа
                if (answer.isDecision() && answer.getContent().equals("No")){


                    event.setMessage("отказ в удалении директории." +
                            " Не получено одобрение других участников, просматривающих контент внутри директории");
                    event.getInternalData().setCompensationPhase(true);
                    return true;
                }
            }
            return false;
        };


        // первая фаза - опрос
        Function<Map<String, TriggerAnswer>, Boolean> activityPollingPhaseStrategy = (answers)->{

            System.out.println("activity check phase "+answers);


            for (TriggerAnswer answer:answers.values()){
                // если обнаружен кто-то, кто не принял решение. ждем его
                if (!answer.isDecision()){
                    return false;
                }
            }
            // если все согласны, то очередь выполняет следующий шаг
            return true;
        };

        // конечная фаза - тут необходимо принять решение о том,
        // продолжать ли цепочку - на основании полученных ответов
        Function<Map<String, TriggerAnswer>, Boolean> finalDecisionPhaseStrategy
                = (answers)->{

            System.out.println("finalDecision "+answers);
            for (TriggerAnswer answer:answers.values()){
                if (!answer.isDecision()){
                    event.setMessage("отказ в удалении директории." +
                            " Не получено одобрение других участников," +
                            " просматривающих контент внутри директории");
                    event.getInternalData().setCompensationPhase(true);
                    return false;
                }
            }

            return true;
        };

        // для каждой фазы проставляется время срабатывания
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
                        event.getExternalData().getId().toString())))
                .build();


        // чтобы система увидела триггер,
        // следующий шаг обязательно должен сопровождаться аннотацией waiting for
        createTrigger(phaseTrigger);
    }


    @Step(name="prepare_directory")
    @Message
    @Next(name = "block_directory")
    @WaitingFor(time = 20)
    public void prepareDirectory(DirectoryRemovalEvent event){
        event.setMessage("Согласование завершено. Резервируем сущность");
        transaction().execute(status -> {
            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getId());
            if (directoryCheck.isEmpty() || directoryCheck.get().getStatus()!=DirectoryStatus.AVAILABLE){
                throw new IllegalStateException("Директория занята другим процессом");

            }


            // данный статус не позволит никому сверху или снизу в иерархии что либо изменить
            directoryCheck.get().setStatus(DirectoryStatus.PREPARING_FOR_REMOVAL);
            return null;
        });



        try {
            // инвалидируем структуру
            hotLayer.projectStructureInvalidation(
                    new ProjectStructureInvalidation(event.getContext().getProjectId()));
        }

        catch (Exception e){

        }


    }


    // блокировка директории по статусу - защищает от некоторых параллельных действий
    // над родительскими и дочерними структурами
    @Step(name = "block_directory")
    @Next(name = "remove_from_storage")
    @Message
    public void blockDirectory(DirectoryRemovalEvent event){
        event.setMessage("Изолируем директорию");
        transaction().execute(status -> {

            Optional<Directory> check
                    = directoryRepository.findByIdForUpdate(event.getExternalData().getId());
            if (check.isEmpty()) throw new IllegalStateException("директории не существует");




            Directory directory = check.get();




            List<DirectoryReadOnly> parents
                    = snapshotService.getParentsSnapshotDirectoriesOnly(event.getExternalData().getId());

            boolean parentContains = false;
            boolean rootContains = false;

            for (DirectoryReadOnly directoryReadOnly:parents){

                // сам parent
                if (directoryReadOnly.getId().equals(event.getExternalData().getId())){
                    parentContains = true;
                    if (directoryReadOnly.getStatus()!=DirectoryStatus.PREPARING_FOR_REMOVAL){
                        throw new IllegalStateException("Директория не была зарезервирована для блокировки");
                    }

                }
                else {

                    if (directoryReadOnly.getStatus()==DirectoryStatus.REMOVING ||
                            directoryReadOnly.getStatus() == DirectoryStatus.MIGRATING
                            || directoryReadOnly.getStatus() == DirectoryStatus.PREPARING_FOR_REMOVAL
                            || directoryReadOnly.getStatus() == DirectoryStatus.PREPARING_FOR_MIGRATING

                    ){
                        throw new IllegalStateException("используемая папка заблокирована другим процессом");
                    }


                    // root
                    if (directoryReadOnly.getId().equals(event.getInternalData().getProjectRoot())){
                        rootContains = true;
                    }

                }








            }

            if (!(parentContains && rootContains)){
                throw new IllegalStateException("директория не принадлежит проекту");
            }

            // смотрим детей - при удалении они не могут быть затронуты никем
            StructureSnapshot children
                    = snapshotService.getFullChildrenSnapshot(event.getExternalData().getId());
            children.getDirectories().forEach(directoryReadOnly -> {
                if (!directoryReadOnly.getId().equals(event.getExternalData().getId()) && directoryReadOnly.getStatus()!=DirectoryStatus.AVAILABLE){
                    throw new IllegalStateException("Внутренняя часть директории затронута другим процессом");
                }
            });


            children.getFiles().forEach(fileReadOnly -> {
                System.out.println(fileReadOnly.getStatus()+" "+fileReadOnly.getName());
                if (fileReadOnly.getStatus()
                        != FileStatus.AVAILABLE){
                    throw new IllegalStateException("Какой то из файлов внутри директории в данный момент затронут другим процессом");
                }
            });

            directory.setStatus(DirectoryStatus.REMOVING);

            return null;
        });
    }



    //

    @Step(name = "remove_from_storage")
    @Message
    @Next(name = "remove_from_db")
    public void removeFromStorage(DirectoryRemovalEvent directoryRemovalEvent){
        directoryRemovalEvent.setMessage("очищаем хранилище и кеши");
        var toDelete = transaction().execute(status -> snapshotService
                .getAllFilesBelowDirectory(directoryRemovalEvent
                        .getInternalData().getProjectRoot())).stream().map(file->file.getId().toString())
                .toList();


        // очищаем холодное хранилище
        storage.deleteBatch(toDelete);






    }

    @EndingStep(name = "remove_from_db")
    public void removeFromDb(DirectoryRemovalEvent directoryRemovalEvent){
        directoryRemovalEvent.setMessage("очищаем базу данных");

        // каскадное удаление
        var toClear = transaction().execute(status -> {


            List<FileReadOnly> files = snapshotService
                    .getAllFilesBelowDirectory(directoryRemovalEvent
                                    .getInternalData().getProjectRoot());

            directoryRepository.deleteById(directoryRemovalEvent.getExternalData().getId());

            return files;
        }).stream().map(FileReadOnly::getId).toList();




        try {
            // инвалидация кеша файлов и редактирование файлов, содержащих ссылки
            hotLayer.onDirectoryRemoval(

                    DirectoryRemoval.builder()
                            .files(toClear)
                            .build()
            );


            // инвалидируем структуру
            hotLayer.projectStructureInvalidation(
                    new ProjectStructureInvalidation(directoryRemovalEvent.getContext().getProjectId()));


        }

        catch (Exception e){

        }



    }
}
