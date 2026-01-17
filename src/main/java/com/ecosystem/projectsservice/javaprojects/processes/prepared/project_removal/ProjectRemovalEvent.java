package com.ecosystem.projectsservice.javaprojects.processes.prepared.project_removal;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.UserExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.prepared.project_removal.event_structure.ProjectRemovalExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared.project_removal.event_structure.ProjectRemovalInternalData;

@EventQualifier("project_removal")
public class ProjectRemovalEvent extends DeclarativeChainEvent<UserExternalEventContext,
        ProjectRemovalExternalData,
        ProjectRemovalInternalData> {
}
