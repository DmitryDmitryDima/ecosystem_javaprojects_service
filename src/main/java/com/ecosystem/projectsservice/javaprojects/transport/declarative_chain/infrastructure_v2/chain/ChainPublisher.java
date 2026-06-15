package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain;

public interface ChainPublisher {


    void publish(ChainOutput output,
                 OutputMetadata<?> metadata);
}
