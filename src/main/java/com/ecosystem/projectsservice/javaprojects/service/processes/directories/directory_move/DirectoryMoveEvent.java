package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_move;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.events.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;

@EventQualifier("directory_move")
public class DirectoryMoveEvent extends DeclarativeChainEvent<ProjectEventFromUserContext, DirectoryMoveExternalData, DirectoryMoveInternalData> {


}
