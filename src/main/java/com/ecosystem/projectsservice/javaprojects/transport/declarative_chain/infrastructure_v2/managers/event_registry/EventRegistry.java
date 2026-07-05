package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_registry;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;

import java.util.Optional;

public interface EventRegistry {

    void register(Class<? extends ChainEvent> eventClass);


    Optional<Class<? extends ChainEvent>> getRegisteredClass(String type);


}
