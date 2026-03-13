package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_add;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.context_categories.ProjectEventFromUserContext;

@EventQualifier("file_add")
public class FileAddEvent extends DeclarativeChainEvent<ProjectEventFromUserContext, FileAddExternalData, FileAddInternalData> {

}
