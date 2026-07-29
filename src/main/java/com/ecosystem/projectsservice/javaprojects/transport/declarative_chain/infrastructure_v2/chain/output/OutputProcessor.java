package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatar;

public interface OutputProcessor {


    OutputResult output(ChainOutput output,
                        OutputMetadata<?> metadata, ProcessAvatar avatar);


}
