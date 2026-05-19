package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.InternalEventData;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DirectoryAddInternalData extends InternalEventData {




    private UUID projectRoot;
}
