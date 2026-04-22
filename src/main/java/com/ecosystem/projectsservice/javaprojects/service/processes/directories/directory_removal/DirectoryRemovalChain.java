package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_removal;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.StructureSnapshot;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
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
import com.ecosystem.projectsservice.javaprojects.service.projects.SnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

// todo polling фаза в данном случае требует более сложной обработки на фронтенде
// фронтенд должен понять - сидит ли пользователь в каком либо из детей удаляемой директории
// в теории мы можем сформировать и послать список файлов, которые можно считать удаляемыми

// todo ИНВАЛИДАЦИЯ КЕША - В ОСОБЕННОСТИ ВНИМАНИЕ НА БУДУЩИЙ КЕШ ПРЕДЛОЖЕК
@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_REMOVE_DIRECTORY)
public class DirectoryRemovalChain extends ControlledOutboxChain<DirectoryRemovalEvent> {



    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private SnapshotService snapshotService;

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

        // todo в идеале мы должны копировать удаляемый контент на диске в случае,
        //  если удаление диска провалилось.
        String step = event.getInternalData().getCurrentStep();

        if (step.equals("prepare_directory")
                || step.equals("block_directory")
                || step.equals("remove_from_db")){
            transaction().execute(status -> {
                Optional<Directory> directoryCheck = directoryRepository
                        .findByIdForUpdate(event.getExternalData().getId());
                if (directoryCheck.isEmpty()){
                    throw new IllegalStateException("Директории нет");

                }

                directoryCheck.get().setStatus(DirectoryStatus.AVAILABLE);
                return null;
            });
        }

    }


    @OpeningStep(name = "polling")
    @Next(name = "prepare_directory")
    public void polling(DirectoryRemovalEvent event){


        // проверка доступности директории на момент опроса
        Directory directory = transaction().execute(status -> {
            Optional<Directory> initialCheck = directoryRepository.findById(event.getExternalData().getId());
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

            System.out.println("activity check phase");
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
        Function<Map<String, TriggerAnswer>, Boolean> finalDecisionPhaseStrategy = (answers)->{

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
    }


    // блокировка директории по статусу - защищает от некоторых параллельных действий
    // над родительскими и дочерними структурами
    @Step(name = "block_directory")
    @Next(name = "remove_from_db")
    @Message
    public void blockDirectory(DirectoryRemovalEvent event){
        event.setMessage("Изолируем директорию");
        transaction().execute(status -> {

            Optional<Directory> check = directoryRepository.findByIdForUpdate(event.getExternalData().getId());
            if (check.isEmpty()) throw new IllegalStateException("директории не существует");




            Directory directory = check.get();


            // сразу конструируем полный путь
            event.getInternalData()
                    .setFullPath(Path.of(event.getInternalData().getProjectsPath(), directory.getConstructedPath()).normalize().toString());

            List<DirectoryReadOnly> parents = snapshotService.getParentsSnapshotDirectoriesOnly(event.getExternalData().getId());

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
            StructureSnapshot children = snapshotService.getFullChildrenSnapshot(event.getExternalData().getId());
            children.getDirectories().forEach(directoryReadOnly -> {
                if (!directoryReadOnly.getId().equals(event.getExternalData().getId()) && directoryReadOnly.getStatus()!=DirectoryStatus.AVAILABLE){
                    throw new IllegalStateException("Внутренняя часть директории затронута другим процессом");
                }
            });


            children.getFiles().forEach(fileReadOnly -> {
                System.out.println(fileReadOnly.getStatus()+" "+fileReadOnly.getName());
                if (fileReadOnly.getStatus()!= FileStatus.AVAILABLE){
                    throw new IllegalStateException("Какой то из файлов внутри директории в данный момент затронут другим процессом");
                }
            });

            directory.setStatus(DirectoryStatus.REMOVING);

            return null;
        });
    }

    @Step(name = "remove_from_db")
    @Message
    @Next(name = "remove_from_disk")
    public void removeFromDb(DirectoryRemovalEvent directoryRemovalEvent){
        directoryRemovalEvent.setMessage("очищаем базу данных");
        transaction().execute(status -> {

            directoryRepository.deleteById(directoryRemovalEvent.getExternalData().getId());

           return null;
        });
    }

    @EndingStep(name = "remove_from_disk")
    public void removeFromDisk(DirectoryRemovalEvent directoryRemovalEvent){
        directoryRemovalEvent.setMessage("подчищаем диск");
        try {
            FileSystemUtils.deleteRecursively(Path.of(directoryRemovalEvent.getInternalData().getFullPath()));
        }
        catch (Exception e){
            throw new IllegalStateException("ошибка очистки диска");
        }
    }
}
