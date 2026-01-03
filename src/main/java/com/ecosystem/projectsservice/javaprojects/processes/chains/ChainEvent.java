package com.ecosystem.projectsservice.javaprojects.processes.chains;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.context.ApplicationEvent;


@Setter
@Getter
@SuperBuilder
public abstract class ChainEvent {

    private long outboxParent = -1; // сущность, породившая ивент

    private long currentRetry = -1;

    public ChainEvent(){

    }





}
