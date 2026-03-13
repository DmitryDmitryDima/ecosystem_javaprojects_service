package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_removal;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.context_categories.ProjectEventFromUserContext;
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
