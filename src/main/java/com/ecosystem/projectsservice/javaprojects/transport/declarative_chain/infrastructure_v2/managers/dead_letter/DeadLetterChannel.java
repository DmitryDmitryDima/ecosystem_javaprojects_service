package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.dead_letter;

public interface DeadLetterChannel {



    void send(DeadLetter deadLetter);
}
