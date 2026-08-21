package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.managers.sender;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.sender.ChainManagerSender;
import org.springframework.context.ApplicationEventPublisher;


public class ChainManagerSenderSpringApplicationPublisherAdapter implements ChainManagerSender {



    private ApplicationEventPublisher standardEventPublisher;

    public ChainManagerSenderSpringApplicationPublisherAdapter(
            ApplicationEventPublisher standardEventPublisher) {
        this.standardEventPublisher = standardEventPublisher;
    }

    @Override
    public void send(ChainEvent event) {
        standardEventPublisher.publishEvent(event);
    }
}
