package com.ecosystem.projectsservice.javaprojects.service.processes.dead_letter;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.dead_letter.DeadLetter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class DeadLetterListener {


    @EventListener
    public void listenDeadLetterChannel(DeadLetter deadLetter){

        System.out.println("dead letter received ");
        System.out.println(deadLetter.getMessage());
    }
}
