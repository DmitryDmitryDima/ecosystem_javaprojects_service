package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorage;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.mapper.MapperComponent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.sender.ChainManagerSender;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModel;

public class EventManagerDefault implements EventManager{

    // исходящий канал для расшифрованных ивентов
    private ChainManagerSender sender;

    // источник классов для расшифровки
    private EventRegistry registry;


    // mapper
    private MapperComponent mapperComponent;


    private ProcessRuntimeStorage processRuntimeStorage;




    public EventManagerDefault(){}

    public EventManagerDefault(ChainManagerSender sender,
                               EventRegistry registry,
                               MapperComponent mapper, ProcessRuntimeStorage runtimeStorage){
        this.sender = sender;
        this.registry = registry;
        this.mapperComponent = mapper;
        this.processRuntimeStorage = runtimeStorage;
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

    @Override
    public ManagementResult workWithWaitingEvent(OutboxModel model) {
        return null;
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
