package com.ecosystem.projectsservice.javaprojects.service.processes.files.filesave;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;
import lombok.Setter;
import lombok.ToString;

@EventQualifier("file_save")
@Setter
@ToString
public class FileSaveEvent extends DeclarativeChainEvent<ProjectEventFromUserContext, FileSaveExternalData, FileSaveInternalData> {









}
