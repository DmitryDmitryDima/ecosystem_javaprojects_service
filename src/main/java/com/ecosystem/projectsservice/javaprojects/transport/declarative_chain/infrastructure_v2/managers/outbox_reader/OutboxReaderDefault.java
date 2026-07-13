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


    @Override
    public void readWaitingEvents() {

        // атомарно проставлен processing статус
        List<OutboxModel> actualWaiting
                = repository.readActualWaitingEvents();


        for (var model:actualWaiting){

            ManagementResult result = manager.workWithWaitingEvent(model);

            if (!result.isSuccess()){

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
        }







    }

    @Override
    public void readExpiredWaitingEvents() {

    }

    @Override
    public void readExpiredProcessingEvents() {

    }

    @Override
    public void readEverlastingProcessingEvents() {

    }

    @Override
    public void readMissedExpiredProcessingEvents() {

    }

    @Override
    public void readManagerCrashedEvents() {

    }

    @Override
    public void readWaitingForSignalEvents() {

    }
}
