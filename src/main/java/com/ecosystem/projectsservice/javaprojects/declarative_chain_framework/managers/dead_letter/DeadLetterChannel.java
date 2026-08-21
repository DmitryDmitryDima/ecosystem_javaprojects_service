package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.dead_letter;

public interface DeadLetterChannel {



    void send(DeadLetter deadLetter);
}
