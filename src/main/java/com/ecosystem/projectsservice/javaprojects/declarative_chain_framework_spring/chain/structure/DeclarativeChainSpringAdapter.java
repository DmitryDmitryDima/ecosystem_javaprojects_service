package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.chain.structure;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.DeclarativeChain;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output.OutputProcessor;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.exception.ChainPreparationException;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.avatar.ProcessAvatarStorage;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events.ChainEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DeclarativeChainSpringAdapter <E extends ChainEvent>
        extends DeclarativeChain<E> {


    @Override
    @Autowired
    protected void setOutputProcessor( OutputProcessor outputProcessor) {
        super.setOutputProcessor(outputProcessor);
    }

    @Override
    @Autowired
    protected void setEventRegistry( EventRegistry eventRegistry) {
        super.setEventRegistry(eventRegistry);
    }

    @Override
    @Autowired
    protected void setProcessRuntimeStorage( ProcessAvatarStorage processAvatarStorage) {
        super.setProcessRuntimeStorage(processAvatarStorage);
    }

    @PostConstruct
    @Override
    public void prepareChain() throws ChainPreparationException {
        super.prepareChain();

    }
}
