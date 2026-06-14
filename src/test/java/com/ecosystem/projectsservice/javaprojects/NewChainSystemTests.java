package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add.DirectoryAddExternalData;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.events.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEventProcessingInfo;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ExternallyConnectedChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test.DirectoryAddTestEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@SpringBootTest
public class NewChainSystemTests {

    @Autowired
    private ObjectMapper mapper;



    @Test
    public void transform(){
        DirectoryAddTestEvent event = new DirectoryAddTestEvent();

        EventQualifier type = event.getClass().getAnnotation(EventQualifier.class);

        Class<?> clazz = event.getClass();

        event.setProcessingInfo(new ChainEventProcessingInfo());
        event.getProcessingInfo().setCurrentStep("step 1");

        event.setMessage("hello");


        event.setExternalContext(ProjectEventFromUserContext.builder()
                .username("dima")
                .userUUID(UUID.randomUUID())
                .renderId(UUID.randomUUID()).projectId(UUID.randomUUID())
                .correlationId(UUID.randomUUID()).timestamp(Instant.now())
                .alarmStrategy(null).notificationStrategy(null).build());


        event.setOutboxId(UUID.randomUUID());


        DirectoryAddExternalData data = new DirectoryAddExternalData();
        data.setId(UUID.randomUUID());
        data.setName("new dir");
        data.setParentId(UUID.randomUUID());


        event.setExternalData(data);



        String outboxData = mapper.writeValueAsString(event);


        // пытаемся прочитать




        ChainEvent read = (ChainEvent) mapper.readValue(outboxData,
                clazz);

        System.out.println(read.getMessage());

        System.out.println(read.getProcessingInfo().getCurrentStep());

        ExternallyConnectedChainEvent<?,?> transform
                = (ExternallyConnectedChainEvent) read;

        System.out.println(transform.getExternalContext().getTimestamp());


        DirectoryAddTestEvent event1 = (DirectoryAddTestEvent) mapper.readValue(outboxData,
                clazz);

        System.out.println(event1.getExternalData().getName());



    }
}
