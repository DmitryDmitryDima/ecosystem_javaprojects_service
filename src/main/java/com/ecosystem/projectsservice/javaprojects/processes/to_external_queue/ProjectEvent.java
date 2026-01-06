package com.ecosystem.projectsservice.javaprojects.processes.to_external_queue;

import com.ecosystem.projectsservice.javaprojects.processes.chains.EventName;
import com.ecosystem.projectsservice.javaprojects.processes.chains.Retryable;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Retryable(count = 3)
@EventName("outbox_project_event_for_external")
public class ProjectEvent extends BasicQueueEvent {


    private ExternalEventContext context;

    private ExternalEventData eventData;

    public ProjectEvent(){
        super();
    }






}
