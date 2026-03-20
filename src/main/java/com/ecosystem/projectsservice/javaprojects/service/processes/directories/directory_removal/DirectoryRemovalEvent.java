package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_removal;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;

@EventQualifier("directory_remove")
public class DirectoryRemovalEvent extends DeclarativeChainEvent<ProjectEventFromUserContext,
        DirectoryRemovalExternalData,
        DirectoryRemovalInternalData> {

}
