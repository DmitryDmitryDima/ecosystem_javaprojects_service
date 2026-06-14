package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events;

import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;

public interface ExternallyConnected <C extends ExternalEventContext,
        D extends ExternalEventData> {








    C getContext();


    D getData();


}
