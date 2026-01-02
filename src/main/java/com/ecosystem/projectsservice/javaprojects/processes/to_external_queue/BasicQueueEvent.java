package com.ecosystem.projectsservice.javaprojects.processes.to_external_queue;


import com.ecosystem.projectsservice.javaprojects.processes.chains.ChainEvent;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public abstract class BasicQueueEvent extends ChainEvent {
    protected String event_type;
    protected String message;



}
