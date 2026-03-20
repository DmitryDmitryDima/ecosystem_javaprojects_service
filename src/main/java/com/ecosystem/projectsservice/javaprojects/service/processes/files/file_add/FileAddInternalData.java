package com.ecosystem.projectsservice.javaprojects.service.processes.files.file_add;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.InternalEventData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileAddInternalData extends InternalEventData {

    private String projectsPath;
    private String filepath;

    private long projectRoot;
}
