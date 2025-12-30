package com.ecosystem.projectsservice.javaprojects.processes.external_queue;


import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public abstract class BasicQueueEvent {
    protected String event_type;
    protected String message;



}
