package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.sender;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;


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
