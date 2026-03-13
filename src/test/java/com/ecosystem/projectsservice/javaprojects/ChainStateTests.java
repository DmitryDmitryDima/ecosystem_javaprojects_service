package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.testing.ControlTestChain;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.ProcessAggregator;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.TriggersAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ChainStateTests {

    @Autowired
    private ControlTestChain controlTestChain;

    @Autowired
    private ProcessAggregator aggregator;

    @Autowired
    private TriggersAggregator triggersAggregator;





}
