package com.ecosystem.projectsservice.javaprojects.processes.queue;


import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class UserEvent extends BasicQueueEvent {


    private UserEventContext context;

    private EventData eventData;






}
