package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.publisher;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.ChainOutput;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.OutputMetadata;

public interface ChainPublisher {


    void publish(ChainOutput output,
                 OutputMetadata<?> metadata);


}
