package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.sender;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class ChainManagerSenderSpringApplicationPublisherAdapter implements ChainManagerSender {


    @Autowired
    private ApplicationEventPublisher standardEventPublisher;


    @Override
    public void send(ChainEvent event) {
        standardEventPublisher.publishEvent(event);
    }
}
