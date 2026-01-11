package com.ecosystem.projectsservice.javaprojects.processes.to_external_queue;


import com.ecosystem.projectsservice.javaprojects.processes.chains.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.EventStatus;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public abstract class BasicQueueEvent extends ChainEvent {
    protected String event_type;
    protected String message;
    protected EventStatus status;

    public BasicQueueEvent(){
        super();
    }



}
