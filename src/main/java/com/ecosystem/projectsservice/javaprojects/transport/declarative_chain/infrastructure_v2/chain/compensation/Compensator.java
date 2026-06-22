package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.compensation;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;

public interface Compensator <E extends ChainEvent> {

    void compensate(E event);
}
