package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test;

import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_removal.DirectoryRemovalExternalData;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.events.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ExternallyConnectedChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;


@EventQualifier("directory_add_test")
public class DirectoryRemoveTestEvent
        extends ExternallyConnectedChainEvent<ProjectEventFromUserContext,
        DirectoryRemovalExternalData> {
}
