package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_add;


import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.InternalEventData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileAddInternalData extends InternalEventData {

    private String projectsPath;
    private String filepath;

    private long projectRoot;
}
