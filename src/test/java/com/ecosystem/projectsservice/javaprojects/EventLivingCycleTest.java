package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add.DirectoryAddExternalData;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.ChainOutput;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.OutputMetadata;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.OutputProcessor;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.output_actions.ChainInit;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxStatus;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test.DirectoryAddTestEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.UUID;

@SpringBootTest
public class EventLivingCycleTest {


    @Autowired
    private OutputProcessor outputProcessor;




    @Test
    public void process(){


        ChainEvent event = getChainEvent();

        ChainOutput output = new ChainOutput();

        OutputMetadata<?> meta = new OutputMetadata<>();

        meta.setAction(new ChainInit()); // инициализация



        output.setEvent(event);
        output.setStatus(OutboxStatus.WAITING);
        output.setLast_update(Instant.now());
        output.setReadExpiration(output.getLast_update().plusSeconds(30));
        output.setPerformanceExpirationPeriod(10000L);



        outputProcessor.output(output, meta);








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
