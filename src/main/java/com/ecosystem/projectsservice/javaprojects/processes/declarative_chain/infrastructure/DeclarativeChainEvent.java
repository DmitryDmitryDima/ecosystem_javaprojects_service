package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure;


import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public abstract class DeclarativeChainEvent <

        Context extends ExternalEventContext,
        External extends ExternalEventData,
        Internal extends InternalEventData

        > {






    private Context context;

    private String message;

    private External externalData;

    private Internal internalData;























}
