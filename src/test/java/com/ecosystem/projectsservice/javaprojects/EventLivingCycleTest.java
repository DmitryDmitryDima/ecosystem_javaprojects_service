package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add.DirectoryAddExternalData;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output.OutputProcessor;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.mapper.MapperComponent;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModel;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModelDefault;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModelRepository;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxStatus;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.UUID;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventLivingCycleTest {


    @Autowired
    private OutputProcessor outputProcessor;

    /*
    @Autowired
    private OutboxModelRepository repository;

     */

    @Autowired
    private OutboxModelRepository repository;

    @Autowired
    private MapperComponent mapper;











    @Test
    @Order(2)
    public void repository(){


        System.out.println(repository.readMissedExpiredProcessingEvents());




    }


    @Test
    @Order(1)
    public void outputProcessor(){











    }


    private OutboxModel getModel(){
        ChainEvent event = getChainEvent();


        OutboxModelDefault model = new OutboxModelDefault();


        model.setPerformanceLimitTime(null);
        model.setLastUpdate(Instant.now());
        model.setPayload(mapper.writeValueAsString(event));
        model.setType("directory_add_test");
        model.setProcessUUID(event.getProcessId());

        model.setReadExpiration(Instant.now().plusSeconds(60*60));

        model.setStatus(OutboxStatus.WAITING);

        return model;
    }





    private ChainEvent getChainEvent(){





        DirectoryAddTestEvent testEvent = new DirectoryAddTestEvent();

        testEvent.setMessage("Hello i am test event");

        testEvent.setProcessId(UUID.fromString("019f7b9e-8712-7f01-b217-5bba7d96290a"));

        ProjectEventFromUserContext externalContext = new ProjectEventFromUserContext();

        externalContext.setUsername("user");
        externalContext.setUserUUID(UUID.randomUUID());
        externalContext.setRenderId(UUID.randomUUID());
        externalContext.setCorrelationId(testEvent.getProcessId());





        testEvent.setExternalContext(externalContext );


        DirectoryAddExternalData data = new DirectoryAddExternalData(UUID.randomUUID(),
                "new_folder", UUID.randomUUID());



        testEvent.setExternalData(data);



        return testEvent;






    }
}
