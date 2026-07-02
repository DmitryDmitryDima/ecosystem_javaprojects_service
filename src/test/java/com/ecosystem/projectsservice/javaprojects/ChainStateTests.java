package com.ecosystem.projectsservice.javaprojects;



import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.OutputProcessor;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

@SpringBootTest
public class ChainStateTests {





    @Autowired
    private EventRegistry eventRegistry;

    @Autowired
    private ProcessRuntimeStorage storage;

    @Autowired
    private OutputProcessor publisher;



    @Autowired
    private ApplicationEventPublisher eventPublisher;






    @Test
    public void test(){










    }







}
