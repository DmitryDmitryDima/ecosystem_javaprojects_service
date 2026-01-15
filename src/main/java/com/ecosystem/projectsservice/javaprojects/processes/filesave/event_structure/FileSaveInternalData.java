package com.ecosystem.projectsservice.javaprojects.processes.filesave.event_structure;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.InternalEventData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileSaveInternalData extends InternalEventData {

    private String filePath;
    private String projectsPath;
}
