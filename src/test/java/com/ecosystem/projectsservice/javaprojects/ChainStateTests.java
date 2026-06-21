package com.ecosystem.projectsservice.javaprojects;



import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.publisher.ChainPublisher;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.exception.ChainPreparationException;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorage;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

@SpringBootTest
public class ChainStateTests {


    @Autowired
    private BasicSpringEdition basic;


    @Autowired
    private EventRegistry eventRegistry;

    @Autowired
    private ProcessRuntimeStorage storage;

    @Autowired
    private ChainPublisher publisher;



    @Autowired
    private ApplicationEventPublisher eventPublisher;






    @Test
    public void test(){


        DirectoryAddTestEvent testEvent = new DirectoryAddTestEvent();

        testEvent.setMessage("hello directory add");

        DirectoryRemoveTestEvent testEvent1 = new DirectoryRemoveTestEvent();

        testEvent1.setMessage("hello directory remove");

        eventPublisher.publishEvent(testEvent);



        eventPublisher.publishEvent(testEvent1);







    }







}
