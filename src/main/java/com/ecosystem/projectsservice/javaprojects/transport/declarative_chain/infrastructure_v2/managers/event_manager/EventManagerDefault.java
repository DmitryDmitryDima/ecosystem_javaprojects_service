package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorage;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.status_groups.DeliveryStatus;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.dead_letter.DeadLetterChannel;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.mapper.MapperComponent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.sender.ChainManagerSender;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModel;

import java.util.Optional;


// TODO при работе с event manager не забываем корректно проставлять delivery status
public class EventManagerDefault implements EventManager{

    // исходящий канал для расшифрованных ивентов
    private ChainManagerSender sender;

    // источник классов для расшифровки
    private EventRegistry registry;


    // mapper
    private MapperComponent mapperComponent;


    private ProcessRuntimeStorage processRuntimeStorage;

    private DeadLetterChannel deadLetterChannel;




    public EventManagerDefault(){}

    public EventManagerDefault(ChainManagerSender sender,
                               EventRegistry registry,
                               MapperComponent mapper,
                               ProcessRuntimeStorage runtimeStorage,
                               DeadLetterChannel deadLetterChannel
                               ){
        this.sender = sender;
        this.registry = registry;
        this.mapperComponent = mapper;
        this.processRuntimeStorage = runtimeStorage;
        this.deadLetterChannel = deadLetterChannel;
    }






    public ChainManagerSender getSender() {
        return sender;
    }

    public void setSender(ChainManagerSender sender) {
        this.sender = sender;
    }

    public EventRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(EventRegistry registry) {
        this.registry = registry;
    }

    public MapperComponent getMapperComponent() {
        return mapperComponent;
    }

    public void setMapperComponent(MapperComponent mapperComponent) {
        this.mapperComponent = mapperComponent;
    }


    public ProcessRuntimeStorage getProcessRuntimeStorage() {
        return processRuntimeStorage;
    }

    public void setProcessRuntimeStorage(ProcessRuntimeStorage processRuntimeStorage) {
        this.processRuntimeStorage = processRuntimeStorage;
    }

    public void setDeadLetterChannel(DeadLetterChannel deadLetterChannel) {
        this.deadLetterChannel = deadLetterChannel;
    }

    public DeadLetterChannel getDeadLetterChannel() {
        return deadLetterChannel;
    }



    private ChainEvent readPayload(OutboxModel model){

        // проверяем тип
        String type = model.getType();

        if (type == null){
            throw new EventManagerException("в прочитанной outbox" +
                    " модели отсутствует тип, чтение состояния процесса невозможно");

        }

        String payload = model.getPayload();

        if (payload == null){
            throw new EventManagerException("в прочитанной outbox модели не записан ивент," +
                    " чтение состояние я процесса невозможно");
        }

        Optional<Class<? extends ChainEvent>> clazzCheck
                = registry.getRegisteredClass(type);

        if (clazzCheck.isEmpty()){
            throw new EventManagerException("тип," +
                    " указанный в прочитанной outbox модели, не был зарегистрирован." +
                    " Чтение состояния процесса невозможно");
        }

        return mapperComponent.read(payload,
                clazzCheck.get());





    }



    // в данном случае аватар может отсутствовать, поэтому проверку не проводим

    @Override
    public ManagementResult workWithWaitingEvent(OutboxModel model) {


        try {

            ChainEvent chainEvent = readPayload(model);

            chainEvent.getProcessingInfo()
                    .setDeliveryStatus(DeliveryStatus.SUCCESS_READING);

            sender.send(chainEvent);

            return new ManagementResult(true, null);







        }

        // ошибка менеджера

        catch (Exception exception){
            return new ManagementResult(false, exception);


        }




    }

    @Override
    public ManagementResult workWithExpiredWaitingEvent(OutboxModel model) {
        return null;
    }

    @Override
    public ManagementResult workWithEverlastingProcessingEvent(OutboxModel model) {
        return null;
    }

    @Override
    public ManagementResult workWithExpiredProcessingEvent(OutboxModel model) {
        return null;
    }

    @Override
    public ManagementResult workWithMissedExpiredProcessingEvent(OutboxModel model) {
        return null;
    }

    @Override
    public ManagementResult workWithManagerCrashEvent(OutboxModel model) {
        return null;
    }

    @Override
    public ManagementResult workWithExpiredWaitingForSignalEvent(OutboxModel model) {
        return null;
    }
}
