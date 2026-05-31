package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.events.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;

@EventQualifier("directory_add")
public class DirectoryAddEvent extends DeclarativeChainEvent<ProjectEventFromUserContext, DirectoryAddExternalData, DirectoryAddInternalData> {
}
