package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.event_registry;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;

public interface EventRegistry {

    void register(Class<? extends ChainEvent> eventClass);


}
