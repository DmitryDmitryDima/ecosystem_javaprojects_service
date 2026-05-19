package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_removal;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.InternalEventData;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DirectoryRemovalInternalData extends InternalEventData {


    private UUID projectRoot;

}
