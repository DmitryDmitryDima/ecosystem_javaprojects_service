package com.ecosystem.projectsservice.javaprojects;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.filesave.FileSaveEvent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

@SpringBootTest
public class DeclarativeChainTest {

    @Autowired
    private ApplicationEventPublisher publisher;


    @Test
    public void test(){
        publisher.publishEvent(new FileSaveEvent());

    }

}
