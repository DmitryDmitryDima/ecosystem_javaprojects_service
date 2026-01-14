package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.prepared_chains.filesave;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.prepared_chains.filesave.event_structure.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.prepared_chains.filesave.event_structure.FileSaveInternalData;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ProjectExternalEventContext;
import lombok.Setter;
import lombok.ToString;

@EventQualifier("file_save")
@Setter
@ToString
public class FileSaveEvent extends DeclarativeChainEvent<ProjectExternalEventContext, FileSaveExternalData, FileSaveInternalData> {









}
