package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_removal;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.InternalEventData;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class FileRemovalInternalData extends InternalEventData {

    private String filePath;
    private String projectsPath;
    private Long projectRoot;

}
