package com.ecosystem.projectsservice.javaprojects.service.processes.files.filesave;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.InternalEventData;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FileSaveInternalData extends InternalEventData {

    //private String filePath;
    //private String projectsPath;
    private UUID projectRoot;

}
