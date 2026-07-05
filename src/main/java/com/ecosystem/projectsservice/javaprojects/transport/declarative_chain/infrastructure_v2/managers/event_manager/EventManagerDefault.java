package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager;

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




    public EventManagerDefault(){}

    public EventManagerDefault(ChainManagerSender sender,
                               EventRegistry registry,
                               MapperComponent mapper){
        this.sender = sender;
        this.registry = registry;
        this.mapperComponent = mapper;
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

    @Override
    public void workWithExpiredWaitingForSignalEvent(OutboxModel model) {

    }

    @Override
    public void workWithExpiredProcessingEvent(OutboxModel model) {

    }

    @Override
    public void workWithExpiredWaitingEvent(OutboxModel model) {

    }

    @Override
    public void workWithWaitingEvent(OutboxModel model) {

    }
}
