package com.ecosystem.projectsservice.javaprojects.service.processes.test_processes;

import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add.DirectoryAddExternalData;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.registry.ChainEventQualifier;
import com.ecosystem.projectsservice.javaprojects.external_messaging.events.ExternallyConnectedChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;

@ChainEventQualifier("directory_add_test")
public class DirectoryAddTestEvent
        extends ExternallyConnectedChainEvent<ProjectEventFromUserContext,
                DirectoryAddExternalData
                > {






}
