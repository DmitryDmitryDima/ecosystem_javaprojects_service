package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.OutputProcessor;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.DeclarativeChain;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorage;

public class TestChain extends DeclarativeChain<DirectoryAddTestEvent> {


    public TestChain(ProcessRuntimeStorage runtimeStorage,
                     EventRegistry eventRegistry, OutputProcessor outputProcessor) {
        super(runtimeStorage, eventRegistry, outputProcessor);
    }



    @Override
    public void catchEvent(DirectoryAddTestEvent event) {

    }

    @Override
    protected void compensationStrategy(DirectoryAddTestEvent event) {

    }
}
