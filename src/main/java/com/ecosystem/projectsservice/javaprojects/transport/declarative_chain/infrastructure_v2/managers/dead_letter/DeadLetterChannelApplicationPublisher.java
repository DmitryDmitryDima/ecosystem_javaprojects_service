package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.dead_letter;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModel;
import org.springframework.context.ApplicationEventPublisher;

public class DeadLetterChannelApplicationPublisher implements DeadLetterChannel {

    private ApplicationEventPublisher springPublisher;

    public DeadLetterChannelApplicationPublisher(ApplicationEventPublisher publisher){
        this.springPublisher = publisher;
    }

    @Override
    public void send(DeadLetter deadLetter) {



        springPublisher.publishEvent(deadLetter);
    }
}
