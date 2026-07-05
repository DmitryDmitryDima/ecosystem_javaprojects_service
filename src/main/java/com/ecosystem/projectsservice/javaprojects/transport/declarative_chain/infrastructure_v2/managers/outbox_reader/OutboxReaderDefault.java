package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.outbox_reader;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModelRepository;

public class OutboxReaderDefault implements OutboxReader{


    private OutboxModelRepository repository;

    public OutboxReaderDefault(){}

    public OutboxReaderDefault(OutboxModelRepository repository){
        this.repository = repository;
    }


    public void setRepository(OutboxModelRepository repository) {
        this.repository = repository;
    }
}
