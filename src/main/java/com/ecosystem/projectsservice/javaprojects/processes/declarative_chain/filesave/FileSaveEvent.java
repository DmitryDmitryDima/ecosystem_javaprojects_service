package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.filesave;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.InternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.filesave.event_structure.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.filesave.event_structure.FileSaveInternalData;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.ProjectExternalEventContext;


public class FileSaveEvent extends DeclarativeChainEvent<ProjectExternalEventContext> {

    private FileSaveExternalData externalData;

    private FileSaveInternalData internalData;




    @Override
    public InternalEventData getInternalEventData() {
        return internalData;
    }

    @Override
    public ExternalEventData getExternalEventData() {
        return externalData;
    }
}
