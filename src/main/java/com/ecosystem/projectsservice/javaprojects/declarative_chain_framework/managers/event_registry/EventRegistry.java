package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.event_registry;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events.ChainEvent;

import java.util.Optional;

public interface EventRegistry {

    void register(Class<? extends ChainEvent> eventClass);


    Optional<Class<? extends ChainEvent>> getRegisteredClass(String type);


}
