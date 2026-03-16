package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_move;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.context_categories.ProjectEventFromUserContext;
import lombok.Setter;
import lombok.ToString;

@EventQualifier(value = "file_move")
@Setter
@ToString
public class FileMoveEvent extends DeclarativeChainEvent<ProjectEventFromUserContext, FileMoveExternalData, FileMoveInternalData> {
}
