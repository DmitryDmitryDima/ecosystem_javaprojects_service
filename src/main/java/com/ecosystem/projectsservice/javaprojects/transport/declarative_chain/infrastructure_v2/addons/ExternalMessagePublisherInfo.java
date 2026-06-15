package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.addons;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ExternallyConnectedChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalMessagePublisherInfo {

    // отсюда же берем event qualifier
    private ExternalEvent<? extends ExternalEventContext> externalEventCategory;

    private ExternalEventType externalEventType;

    private ExternallyConnectedChainEvent<? extends ExternalEventContext,
            ? extends ExternalEventData> event;




}
