package com.ecosystem.projectsservice.javaprojects.processes.chains;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.context.ApplicationEvent;


@Setter
@Getter
public abstract class ChainEvent extends ApplicationEvent {

    protected long outboxParent = -1; // сущность, породившая ивент
    protected String eventName;

    public ChainEvent(Object source){
        super(source);
    }


}
