package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output;

public interface OutputProcessor {


    void output(ChainOutput output,
                OutputMetadata<?> metadata);


}
