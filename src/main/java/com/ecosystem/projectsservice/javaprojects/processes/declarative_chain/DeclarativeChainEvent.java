package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain;


import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventData;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public abstract class DeclarativeChainEvent <C extends ExternalEventContext> {






    private C context;

    private String message;




    public abstract InternalEventData getInternalData();

    public abstract ExternalEventData getExternalData();















}
