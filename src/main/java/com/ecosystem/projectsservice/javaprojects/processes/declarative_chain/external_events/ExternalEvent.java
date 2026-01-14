package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@ToString
public class ExternalEvent <C extends ExternalEventContext> {



    private String message;
    private String type;
    private EventStatus status;
    @JsonIgnore
    private Long outboxParent;



    // raw json data
    private String data;

    private C context;






}
