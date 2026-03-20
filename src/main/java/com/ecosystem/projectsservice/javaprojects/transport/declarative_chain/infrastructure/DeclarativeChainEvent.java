package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure;


import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
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
