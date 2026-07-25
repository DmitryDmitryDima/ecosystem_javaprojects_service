package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.outbox_reader;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager.EventManager;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager.ManagementResult;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModel;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModelRepository;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxStatus;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;


// TODO ввести подгрузку параметров времени из файла конфигурации
public class OutboxReaderDefault implements OutboxReader{






    private OutboxModelRepository repository;

    private EventManager manager;

    public OutboxReaderDefault(){}

    public OutboxReaderDefault(OutboxModelRepository repository,
                               EventManager manager){
        this.repository = repository;
        this.manager = manager;
    }


    public void setRepository(OutboxModelRepository repository) {
        this.repository = repository;
    }

    public void setManager(EventManager manager){
        this.manager = manager;
    }


    // read version должен совпадать
    private void attemptToSetManagerCrashedStatus(ManagementResult result,
                                                  OutboxModel model){

        // блокирующе ставим статус manager_crash,
        // при этом генерируя message для будущей dead letter
        // менеджер обязан сохранять exception при ошибке
        // при смене статуса не забываем про last_update
        String message = "ошибка обработки ивента "+ result.getException().getMessage();

        repository
                .changeStatusAndMessageForGivenAllReadVersion(model.getOutboxUUID()
                        , OutboxStatus.MANAGER_CRASH, message, model.getAllReadVersion()
                );

    }




    // дефолтное значение после двоеточия
    @Override
    @Scheduled(fixedDelayString = "${reader.waiting.events:500}")
    public void readWaitingEvents() {


        //System.out.println("READING WAITING EVENTS");

        // атомарно проставлен processing статус
        List<? extends OutboxModel> actualWaiting
                = repository.readActualWaitingEvents();
        for (var model:actualWaiting){

            ManagementResult result = manager.workWithWaitingEvent(model);

            if (!result.isSuccess()){

                attemptToSetManagerCrashedStatus(result, model);
            }

        }
    }

    @Override
    @Scheduled(fixedDelayString = "${reader.waiting.events.expired:20000}")
    public void readExpiredWaitingEvents() {


        //System.out.println("READING Expired waiting EVENTS");

        List<? extends OutboxModel> expiredWaitingEvents = repository.readExpiredWaitingEvents();

        for (var model:expiredWaitingEvents){
            ManagementResult managementResult = manager.workWithExpiredWaitingEvent(model);

            if (!managementResult.isSuccess()){

                attemptToSetManagerCrashedStatus(managementResult, model);

            }

            else {
                // компенсационная группа - компенсационный флаг. защищает от сценария,
                // при котором процесс отвис во время запущенной компенсации
                repository.markAsCompensating(model.getOutboxUUID());
            }



        }
    }

    @Override
    @Scheduled(fixedDelayString = "${reader.processing.events.expired:20000}")
    public void readExpiredProcessingEvents() {

        //System.out.println("read expired processing events");


        List<? extends OutboxModel> expiredProcessingEvents
                = repository.readExpiredProcessingEvents();


        for (var model:expiredProcessingEvents){

            ManagementResult result = manager.workWithExpiredProcessingEvent(model);

            if (!result.isSuccess()){
                attemptToSetManagerCrashedStatus(result, model);
            }

            else {
                // компенсационная группа - компенсационный флаг
                repository.markAsCompensating(model.getOutboxUUID());
            }


        }
    }

    @Override
    @Scheduled(fixedDelayString = "${reader.processing.events.everlasting:20000}")
    public void readEverlastingProcessingEvents() {


        //System.out.println("READING everlasting EVENTS");

        List<? extends OutboxModel> everlastingProcessingEvents = repository
                .readEverlastingProcessingEvents();


        for (var model:everlastingProcessingEvents){
            ManagementResult result = manager.workWithEverlastingProcessingEvent(model);

            // внутри менеджера - либо игнор, либо компенсация внутри очереди, либо какая-либо ошибка
            if (!result.isSuccess()){
                attemptToSetManagerCrashedStatus(result, model);
            }

            else {
                if (result.isCompensationStart()){
                    // компенсационная группа - компенсационный флаг.
                    repository.markAsCompensating(model.getOutboxUUID());
                }
            }


        }
    }


    // dead letter статус проставляется атомарно! менеджер не трогает модель и посылает ее в модель
    // 60 секунд
    @Override
    @Scheduled(fixedDelayString = "${reader.processing.events.missed:60000}")
    public void readMissedExpiredProcessingEvents() {


        //System.out.println("READING missed expired EVENTS");

        List<? extends OutboxModel> missedExpiredProcessingEvents
                = repository.readMissedExpiredProcessingEvents();



        for (var model:missedExpiredProcessingEvents){

            manager.workWithMissedExpiredProcessingEvent(model);
        }






    }


    // при чтении данные ивенты атомарно получают финальный dead_letter
    @Override
    @Scheduled(fixedDelayString = "${reader.manager.crashed.events:60000}")
    public void readManagerCrashedEvents() {


        //System.out.println("READING manager crashed EVENTS");

        List<? extends OutboxModel> managerCrashedEvents = repository.readManagerCrashEvents();


        for (var model:managerCrashedEvents){

            manager.workWithManagerCrashEvent(model);
        }

    }

    // атомарно получили processing статус
    @Override
    @Scheduled(fixedDelayString = "${reader.waiting.for.signal.events:2000}")
    public void readExpiredWaitingForSignalEvents() {


        //System.out.println("READING expired waiting for signal EVENTS");


        List<? extends OutboxModel> expiredWaitingForSignalEvents = repository
                .readExpiredWaitingForSignalEvents();

        for (var model:expiredWaitingForSignalEvents){


            ManagementResult result = manager.workWithExpiredWaitingForSignalEvent(model);

            if (!result.isSuccess()){
                attemptToSetManagerCrashedStatus(result, model);
            }

            else {
                // компенсационная группа - компенсационный флаг
                repository.markAsCompensating(model.getOutboxUUID());
            }



        }

    }
}
