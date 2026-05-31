package com.ecosystem.projectsservice.javaprojects.service.processes.files.file_removal;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.events.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;
import lombok.Setter;
import lombok.ToString;

@EventQualifier("file_removal")
@Setter
@ToString
public class FileRemovalEvent extends DeclarativeChainEvent<ProjectEventFromUserContext,
        FileRemovalExternalData,
        FileRemovalInternalData
        >

{
}
