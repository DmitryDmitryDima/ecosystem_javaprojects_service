package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.outbox_reader;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager.EventManager;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModelRepository;

public class OutboxReaderDefault implements OutboxReader{


    private OutboxModelRepository repository;

    private EventManager manager;

    public OutboxReaderDefault(){}

    public OutboxReaderDefault(OutboxModelRepository repository,
                               EventManager manager){
        this.repository = repository;
        this.manager = manager;
    }


    public void setRepository(OutboxModelRepository repository) {
        this.repository = repository;
    }

    public void setManager(EventManager manager){
        this.manager = manager;
    }


    @Override
    public void readWaitingEvents() {

    }

    @Override
    public void readProcessingEvents() {

    }

    @Override
    public void readWaitingForSignalEvents() {

    }
}
