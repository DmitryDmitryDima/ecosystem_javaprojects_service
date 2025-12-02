package com.ecosystem.projectsservice.javaprojects.processes.queue;


import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder
@Data
public abstract class BasicQueueEvent {
    protected String event_type;
    protected String message;



}
