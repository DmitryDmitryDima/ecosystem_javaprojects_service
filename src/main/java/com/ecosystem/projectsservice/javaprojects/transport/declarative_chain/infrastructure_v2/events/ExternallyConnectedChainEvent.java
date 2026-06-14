package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events;


import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ExternallyConnectedChainEvent <C extends ExternalEventContext,
        D extends ExternalEventData>
        extends ChainEvent {


    private C externalContext;

    private D externalData;
}
