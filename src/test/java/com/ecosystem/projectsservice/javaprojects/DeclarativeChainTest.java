package com.ecosystem.projectsservice.javaprojects;

import com.ecosystem.projectsservice.javaprojects.processes.filesave.FileSaveChain;
import com.ecosystem.projectsservice.javaprojects.processes.filesave.FileSaveEvent;

import com.ecosystem.projectsservice.javaprojects.processes.filesave.event_structure.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.filesave.event_structure.FileSaveInternalData;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ProjectExternalEventContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class DeclarativeChainTest {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private FileSaveChain fileSaveChain;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void mappingTest(){
        String payload = """
                {"context":{"timestamp":"2026-01-07T12:28:47.231383300Z","username":"dima","userUUID":"3eb666a1-2b04-4db8-beff-c4b57fea0200","correlationId":"0ab53009-bc9c-4265-9523-7f59c94c18a2","renderId":"60186b1d-bfb5-48c6-8365-5ce08329c2bf","projectId":5,"participants":[]},"message":"starting a chain","externalData":{"fileId":100,"name":"lol","content":"blah blah","path":"/hello"},"internalData":{"currentRetry":0,"currentStep":null,"outboxParent":-1,"filePath":null}}
                """;
        try {
            FileSaveEvent fileSaveEvent = mapper.readValue(payload, FileSaveEvent.class);
            System.out.println(fileSaveEvent.getInternalData().getOutboxParent());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void test(){


        FileSaveEvent mainEvent = new FileSaveEvent();

        ProjectExternalEventContext context = ProjectExternalEventContext.builder()
                .correlationId(UUID.randomUUID())
                .participants(List.of())
                .projectId(5L)
                .renderId(UUID.randomUUID())
                .timestamp(Instant.now())
                .username("dima")
                .userUUID(UUID.randomUUID())
                .build();

        mainEvent.setContext(context);

        FileSaveInternalData internalData = new FileSaveInternalData();
        mainEvent.setInternalData(internalData);

        FileSaveExternalData externalData = new FileSaveExternalData();
        externalData.setContent("blah blah");
        externalData.setName("lol");
        externalData.setPath("/hello");
        externalData.setFileId(100L);

        mainEvent.setExternalData(externalData);

        mainEvent.setMessage("starting a chain");

        try {
            fileSaveChain.init(mainEvent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

}
