package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.outbox_reader;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager.EventManager;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager.ManagerResult;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.outbox.OutboxModel;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.outbox.OutboxModelRepository;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.outbox.OutboxStatus;

import java.util.List;


// TODO
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
    private void attemptToSetManagerCrashedStatus(ManagerResult result,
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

    // попытка мгновенно перевести в dead letter, также смотрим на совпадение версий
    private void attemptToSetDeadLetterStatusInstantly(OutboxModel model){

        System.out.println("instant dead letter status attempt inside reader ");

        repository.changeStatusForGivenAllReadVersion(model.getOutboxUUID(),
                OutboxStatus.DEAD_LETTER, model.getAllReadVersion());
    }




    // дефолтное значение после двоеточия
    @Override
    //@Scheduled(fixedDelayString = "${reader.waiting.events:500}")
    public void readWaitingEvents() {


        //System.out.println("READING WAITING EVENTS");

        // атомарно проставлен processing статус
        List<? extends OutboxModel> actualWaiting
                = repository.readActualWaitingEvents();

        for (var model:actualWaiting){




            ManagerResult result = manager.workWithWaitingEvent(model);

            if (result.getException()!=null){

                attemptToSetManagerCrashedStatus(result, model);
            }

        }
    }

    @Override
    //@Scheduled(fixedDelayString = "${reader.waiting.events.expired:20000}")
    public void readExpiredWaitingEvents() {


        //System.out.println("READING Expired waiting EVENTS");

        List<? extends OutboxModel> expiredWaitingEvents = repository.readExpiredWaitingEvents();

        for (var model:expiredWaitingEvents){
            ManagerResult managementResult = manager.workWithExpiredWaitingEvent(model);

            if (managementResult.getException()!=null){

                attemptToSetManagerCrashedStatus(managementResult, model);

            }


            // compensating флаг выставляется при чтении атомарно для явно компенсационной группы


            /*

            else {
                // компенсационная группа - компенсационный флаг
                // таким образом processing ивент помечается как вошедший в компенсационный сценарий
                repository.markAsCompensating(model.getOutboxUUID());
            }

             */



        }
    }



    // в любом случае - компенсационный сценарий

    @Override
    //@Scheduled(fixedDelayString = "${reader.processing.events.expired:20000}")
    public void readExpiredProcessingEvents() {

        //System.out.println("read expired processing events");


        List<? extends OutboxModel> expiredProcessingEvents
                = repository.readExpiredProcessingEvents();


        for (var model:expiredProcessingEvents){

            ManagerResult result = manager.workWithExpiredProcessingEvent(model);

            if (result.getException()!=null){
                attemptToSetManagerCrashedStatus(result, model);
            }

            if (result.isNeedDeadLetter()){
                attemptToSetDeadLetterStatusInstantly(model);
            }


            // compensating флаг выставляется атомарно для явно компенсационной группы

            /*


            else {


                // компенсационная группа - компенсационный флаг
                repository.markAsCompensating(model.getOutboxUUID());
            }

             */


        }
    }




    @Override
    //@Scheduled(fixedDelayString = "${reader.processing.events.everlasting:20000}")
    public void readEverlastingProcessingEvents() {




        List<? extends OutboxModel> everlastingProcessingEvents = repository
                .readEverlastingProcessingEvents();


        for (var model:everlastingProcessingEvents){
            ManagerResult result = manager.workWithEverlastingProcessingEvent(model);

            // внутри менеджера - либо игнор, либо компенсация внутри очереди, либо какая-либо ошибка
            if (result.getException()!=null){
                attemptToSetManagerCrashedStatus(result, model);
            }

            else {
                if (result.isWithCompensation()){
                    // компенсационная группа - компенсационный флаг.
                    repository.markAsCompensating(model.getOutboxUUID());
                }

                if (result.isNeedDeadLetter()){
                    attemptToSetDeadLetterStatusInstantly(model);
                }
            }


        }
    }


    // dead letter статус проставляется атомарно! менеджер не трогает модель и посылает ее в модель
    // 60 секунд
    @Override
    //@Scheduled(fixedDelayString = "${reader.processing.events.missed:60000}")
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
    //@Scheduled(fixedDelayString = "${reader.manager.crashed.events:60000}")
    public void readManagerCrashedEvents() {


        //System.out.println("READING manager crashed EVENTS");

        List<? extends OutboxModel> managerCrashedEvents = repository.readManagerCrashEvents();


        for (var model:managerCrashedEvents){

            manager.workWithManagerCrashEvent(model);
        }

    }

    // атомарно получили processing статус
    @Override
    //@Scheduled(fixedDelayString = "${reader.waiting.for.signal.events:2000}")
    public void readExpiredWaitingForSignalEvents() {


        //System.out.println("READING expired waiting for signal EVENTS");


        List<? extends OutboxModel> expiredWaitingForSignalEvents = repository
                .readExpiredWaitingForSignalEvents();

        for (var model:expiredWaitingForSignalEvents){


            ManagerResult result = manager.workWithExpiredWaitingForSignalEvent(model);

            if (result.getException()!=null){
                attemptToSetManagerCrashedStatus(result, model);
            }



            // compensating флаг выставляется атомарно для явно компенсационной группы


            /*

            else {
                // компенсационная группа - компенсационный флаг
                repository.markAsCompensating(model.getOutboxUUID());
            }

             */



        }

    }
}
