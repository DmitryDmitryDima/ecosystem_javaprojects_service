package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output.OutputProcessor;

public abstract class Basic {

    private OutputProcessor publisher;

    public Basic(OutputProcessor publisher){
        this.publisher = publisher;
    }


    public OutputProcessor getPublisher() {
        return publisher;
    }
}
