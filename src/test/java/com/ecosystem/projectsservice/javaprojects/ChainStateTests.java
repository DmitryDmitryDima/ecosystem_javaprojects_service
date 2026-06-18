package com.ecosystem.projectsservice.javaprojects;



import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.publisher.ChainPublisher;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.exception.ChainPreparationException;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorage;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test.Basic;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test.BasicSpringEdition;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test.DirectoryAddTestChain;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test.TestChain;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    private DirectoryAddTestChain directoryAddTestChain;

    @Test
    public void test(){


        TestChain testChain = new TestChain(storage, eventRegistry, publisher );


        try {
            testChain.prepareChain();
        } catch (ChainPreparationException e) {
            throw new RuntimeException(e);
        }







    }







}
