package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.directory_removal;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.context_categories.ProjectEventFromUserContext;

@EventQualifier("directory_remove")
public class DirectoryRemovalEvent extends DeclarativeChainEvent<ProjectEventFromUserContext,
        DirectoryRemovalExternalData,
        DirectoryRemovalInternalData> {

}
