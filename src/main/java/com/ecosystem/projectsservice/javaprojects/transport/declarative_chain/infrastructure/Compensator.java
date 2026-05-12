package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure;

import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;


// компенсатор позволяет гибко настраивать стратегии компенсации
// и пути взаимодействия с ее механизмами
public interface Compensator <E extends DeclarativeChainEvent<? extends ExternalEventContext,
        ? extends ExternalEventData,
        ? extends InternalEventData>> {


    void compensation(E event);
}
