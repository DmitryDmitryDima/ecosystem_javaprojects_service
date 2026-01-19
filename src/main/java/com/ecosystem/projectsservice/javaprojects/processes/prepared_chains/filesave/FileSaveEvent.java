package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.filesave;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ProjectEventFromUserContext;
import lombok.Setter;
import lombok.ToString;

@EventQualifier("file_save")
@Setter
@ToString
public class FileSaveEvent extends DeclarativeChainEvent<ProjectEventFromUserContext, FileSaveExternalData, FileSaveInternalData> {









}
