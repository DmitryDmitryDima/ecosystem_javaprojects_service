package com.ecosystem.projectsservice.javaprojects.service.processes.projects_processes.project_removal;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.events.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;

@EventQualifier("project_removal")
public class ProjectRemovalEvent extends DeclarativeChainEvent<ProjectEventFromUserContext,
        ProjectRemovalExternalData,
        ProjectRemovalInternalData> {
}
