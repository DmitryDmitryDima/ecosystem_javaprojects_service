package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.OutputProcessor;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.exception.ChainPreparationException;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorage;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DeclarativeChainSpringAdapter <E extends ChainEvent>
        extends DeclarativeChain<E>{


    @Override
    @Autowired
    protected void setChainPublisher( OutputProcessor outputProcessor) {
        super.setChainPublisher(outputProcessor);
    }

    @Override
    @Autowired
    protected void setEventRegistry( EventRegistry eventRegistry) {
        super.setEventRegistry(eventRegistry);
    }

    @Override
    @Autowired
    protected void setProcessRuntimeStorage( ProcessRuntimeStorage processRuntimeStorage) {
        super.setProcessRuntimeStorage(processRuntimeStorage);
    }

    @PostConstruct
    @Override
    public void prepareChain() throws ChainPreparationException {
        super.prepareChain();

    }
}
