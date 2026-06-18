package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.publisher.ChainPublisher;

public abstract class Basic {

    private ChainPublisher publisher;

    public Basic(ChainPublisher publisher){
        this.publisher = publisher;
    }


    public ChainPublisher getPublisher() {
        return publisher;
    }
}
