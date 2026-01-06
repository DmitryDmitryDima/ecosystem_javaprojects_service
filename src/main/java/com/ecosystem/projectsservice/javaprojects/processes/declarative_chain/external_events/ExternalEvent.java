package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
public class ExternalEvent {



    private String message;
    private String type;



    private ExternalEventData data;
    private ExternalEventContext context;


}
