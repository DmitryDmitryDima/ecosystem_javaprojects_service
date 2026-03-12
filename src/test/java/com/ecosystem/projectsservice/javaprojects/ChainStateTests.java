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





}
