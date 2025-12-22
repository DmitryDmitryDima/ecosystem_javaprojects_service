package com.ecosystem.projectsservice.javaprojects.processes.queue;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class ProjectEvent extends BasicQueueEvent {


    private ProjectEventContext context;

    private EventData eventData;






}
