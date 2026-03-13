package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.project_removal;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.context_categories.ProjectEventFromUserContext;

@EventQualifier("project_removal")
public class ProjectRemovalEvent extends DeclarativeChainEvent<ProjectEventFromUserContext,
        ProjectRemovalExternalData,
        ProjectRemovalInternalData> {
}
