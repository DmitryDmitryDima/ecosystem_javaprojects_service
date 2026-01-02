package com.ecosystem.projectsservice.javaprojects.processes.chains;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompensationEvent extends ChainEvent{

    private String afterEventType;

    private Class<? extends ChainEvent> afterEventTypeConverted;

    public CompensationEvent() {

    }
}
