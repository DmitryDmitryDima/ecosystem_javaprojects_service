package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.outbox_reader;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager.EventManager;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager.ManagementResult;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModel;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModelRepository;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxStatus;

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




    @Override
    public void readWaitingEvents() {

        // атомарно проставлен processing статус
        List<OutboxModel> actualWaiting
                = repository.readActualWaitingEvents();


        for (var model:actualWaiting){

            ManagementResult result = manager.workWithWaitingEvent(model);

            if (!result.isSuccess()){

                attemptToSetManagerCrashedStatus(result, model);







            }




        }

    }

    @Override
    public void readExpiredWaitingEvents() {


        List<OutboxModel> expiredWaitingEvents = repository.readExpiredWaitingEvents();

        for (var model:expiredWaitingEvents){
            ManagementResult managementResult = manager.workWithExpiredWaitingEvent(model);

            if (!managementResult.isSuccess()){

                attemptToSetManagerCrashedStatus(managementResult, model);

            }

            else {
                // компенсационная группа - компенсационный флаг
                repository.markAsCompensating(model.getOutboxUUID());
            }



        }
    }

    @Override
    public void readExpiredProcessingEvents() {


        List<OutboxModel> expiredProcessingEvents
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
    public void readEverlastingProcessingEvents() {

        List<OutboxModel> everlastingProcessingEvents = repository
                .readEverlastingProcessingEvents();


        for (var model:everlastingProcessingEvents){
            ManagementResult result = manager.workWithEverlastingProcessingEvent(model);

            // внутри менеджера - либо игнор, либо компенсация внутри очереди, либо какая-либо ошибка
            if (!result.isSuccess()){
                attemptToSetManagerCrashedStatus(result, model);
            }

            else {
                if (result.isCompensationStart()){
                    // компенсационная группа - компенсационный флаг
                    repository.markAsCompensating(model.getOutboxUUID());
                }
            }


        }
    }


    // dead letter статус проставляется атомарно! менеджер не трогает модель и посылает ее в модель
    @Override
    public void readMissedExpiredProcessingEvents() {

        List<OutboxModel> missedExpiredProcessingEvents
                = repository.readMissedExpiredProcessingEvents();



        for (var model:missedExpiredProcessingEvents){

            manager.workWithMissedExpiredProcessingEvent(model);
        }






    }


    // при чтении данные ивенты атомарно получают финальный dead_letter

    @Override
    public void readManagerCrashedEvents() {

        List<OutboxModel> managerCrashedEvents = repository.readManagerCrashEvents();


        for (var model:managerCrashedEvents){

            manager.workWithManagerCrashEvent(model);
        }

    }

    // атомарно получили processing статус
    @Override
    public void readExpiredWaitingForSignalEvents() {


        List<OutboxModel> expiredWaitingForSignalEvents = repository
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
