package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.directory_removal;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.InternalEventData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectoryRemovalInternalData extends InternalEventData {

    private String projectsPath;
    private Long projectRoot;
    private String fullPath;
}
