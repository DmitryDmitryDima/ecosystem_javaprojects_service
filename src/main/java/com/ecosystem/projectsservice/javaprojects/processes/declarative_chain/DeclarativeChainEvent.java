package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain;


import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventData;
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
