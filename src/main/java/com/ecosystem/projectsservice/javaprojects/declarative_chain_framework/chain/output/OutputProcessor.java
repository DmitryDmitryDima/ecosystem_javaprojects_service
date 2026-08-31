package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.avatar.ProcessAvatar;

public interface OutputProcessor {


    OutputResult output(ChainOutput output,
                        OutputMetadata<?> metadata, ProcessAvatar avatar);


}
