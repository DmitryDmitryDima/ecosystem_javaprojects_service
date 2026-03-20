package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.InternalEventData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectoryAddInternalData extends InternalEventData {

    private String projectsPath;

    private String fullPath;


    private long projectRoot;
}
