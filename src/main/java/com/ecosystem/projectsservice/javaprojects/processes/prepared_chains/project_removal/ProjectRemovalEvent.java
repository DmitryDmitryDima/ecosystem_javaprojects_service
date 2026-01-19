package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.project_removal;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.UserPersonalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.ProjectRemovalExternalData;

@EventQualifier("project_removal")
public class ProjectRemovalEvent extends DeclarativeChainEvent<UserPersonalEventContext,
        ProjectRemovalExternalData,
        ProjectRemovalInternalData> {
}
