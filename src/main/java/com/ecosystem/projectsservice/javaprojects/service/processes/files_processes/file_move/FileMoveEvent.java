package com.ecosystem.projectsservice.javaprojects.service.processes.files_processes.file_move;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.events.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;
import lombok.Setter;
import lombok.ToString;

@EventQualifier(value = "file_move")
@Setter
@ToString
public class FileMoveEvent extends DeclarativeChainEvent<ProjectEventFromUserContext, FileMoveExternalData, FileMoveInternalData> {
}
