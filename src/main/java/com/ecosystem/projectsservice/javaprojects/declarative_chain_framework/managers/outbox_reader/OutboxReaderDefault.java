package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.outbox_reader;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.event_manager.EventManager;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.event_manager.ManagerResult;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.read_limits.ReadLimits;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModel;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModelRepository;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxStatus;

import java.util.List;



public class OutboxReaderDefault implements OutboxReader{






    private OutboxModelRepository repository;

    private EventManager manager;

    private ReadLimits limits;

    public OutboxReaderDefault(){}

    public OutboxReaderDefault(OutboxModelRepository repository,
                               EventManager manager,
                               ReadLimits limits){
        this.repository = repository;
        this.manager = manager;
        this.limits = limits;
    }


    public void setRepository(OutboxModelRepository repository) {
        this.repository = repository;
    }

    public void setManager(EventManager manager){
        this.manager = manager;
    }

    public void setLimits(ReadLimits limits) {
        this.limits = limits;
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
    public void readWaitingEvents() {


        //System.out.println("READING WAITING EVENTS");

        // атомарно проставлен processing статус
        List<? extends OutboxModel> actualWaiting
                = repository.readActualWaitingEvents(limits.waitingEventsLimit());

        for (var model:actualWaiting){




            ManagerResult result = manager.workWithWaitingEvent(model);

            if (result.getException()!=null){

                attemptToSetManagerCrashedStatus(result, model);
            }

        }
    }

    @Override
    public void readExpiredWaitingEvents() {


        //System.out.println("READING Expired waiting EVENTS");

        List<? extends OutboxModel> expiredWaitingEvents
                = repository.readExpiredWaitingEvents();

        for (var model:expiredWaitingEvents){
            ManagerResult managementResult = manager.workWithExpiredWaitingEvent(model);

            if (managementResult.getException()!=null){

                attemptToSetManagerCrashedStatus(managementResult, model);

            }






        }
    }



    // в любом случае - компенсационный сценарий

    @Override
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





        }
    }




    @Override
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
    public void readManagerCrashedEvents() {


        //System.out.println("READING manager crashed EVENTS");

        List<? extends OutboxModel> managerCrashedEvents = repository.readManagerCrashEvents();


        for (var model:managerCrashedEvents){

            manager.workWithManagerCrashEvent(model);
        }

    }

    // атомарно получили processing статус
    @Override
    public void readExpiredWaitingForSignalEvents() {


        //System.out.println("READING expired waiting for signal EVENTS");


        List<? extends OutboxModel> expiredWaitingForSignalEvents = repository
                .readExpiredWaitingForSignalEvents();

        for (var model:expiredWaitingForSignalEvents){


            ManagerResult result = manager.workWithExpiredWaitingForSignalEvent(model);

            if (result.getException()!=null){
                attemptToSetManagerCrashedStatus(result, model);
            }






        }

    }
}
