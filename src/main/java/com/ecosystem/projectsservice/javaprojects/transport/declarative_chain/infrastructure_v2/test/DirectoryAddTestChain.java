package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.publisher.ChainPublisher;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.DeclarativeChainSpringAdapter;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorage;
import org.springframework.stereotype.Service;


@Service
public class DirectoryAddTestChain
        extends DeclarativeChainSpringAdapter<DirectoryAddTestEvent> {




    @Override
    public void catchEvent(DirectoryAddTestEvent event) {

    }

    @Override
    protected void compensationStrategy(DirectoryAddTestEvent event) {

    }



}
