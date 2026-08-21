package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.managers.dead_letter;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.dead_letter.DeadLetter;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.dead_letter.DeadLetterChannel;
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
