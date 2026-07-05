package com.ecosystem.projectsservice.javaprojects;



import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test.DirectoryAddTestChain;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test.DirectoryAddTestEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

@SpringBootTest
public class ChainStateTests {





    @Autowired
    private DirectoryAddTestChain testChain;



    @Autowired
    private ApplicationEventPublisher eventPublisher;






    @Test
    public void test(){


        DirectoryAddTestEvent event = new DirectoryAddTestEvent();

        event.setProcessId(UUID.randomUUID());
        event.setMessage("hello process");



        testChain.init(event);












    }







}
