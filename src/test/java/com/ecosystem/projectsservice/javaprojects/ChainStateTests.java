package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ProjectEventFromUserContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.filesave.FileSaveEvent;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.filesave.FileSaveInternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.testing.ControlTestChain;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.ChainProcess;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.ProcessAggregator;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.TriggerAnswer;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.TriggersAggregator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@SpringBootTest
public class ChainStateTests {

    @Autowired
    private ControlTestChain controlTestChain;

    @Autowired
    private ProcessAggregator aggregator;

    @Autowired
    private TriggersAggregator triggersAggregator;

    @Test
    public void testChainState() throws Exception {


        FileSaveEvent mainEvent = new FileSaveEvent();

        UUID corrId = UUID.randomUUID();

        ProjectEventFromUserContext context = ProjectEventFromUserContext.builder()
                .correlationId(corrId)
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

        controlTestChain.init(mainEvent);

        Thread.ofVirtual().start(()->{
            try {
                while (true){
                    triggersAggregator.notifyTrigger(TriggerAnswer.builder()
                                    .user(UUID.randomUUID())
                                    .content("no")
                                    .decision(true)
                                    .correlationId(corrId)
                            .build());
                    Thread.sleep(10);
                }
            }
            catch (Exception e){

            }
        });




        Thread.sleep(1000000);


        //aggregator.getChainProcessByCorrelationId(context.getCorrelationId()).stop();

        //Thread.sleep(20000);
        System.out.println(aggregator.getChainProcessByCorrelationId(context.getCorrelationId()).getStatus().get());


    }



}
