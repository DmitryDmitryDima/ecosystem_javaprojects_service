package com.ecosystem.projectsservice.javaprojects.service.processes.files.file_add;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.events.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;

@EventQualifier("file_add")
public class FileAddEvent extends DeclarativeChainEvent<ProjectEventFromUserContext, FileAddExternalData, FileAddInternalData> {

}
