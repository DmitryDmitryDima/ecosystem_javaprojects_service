package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.filesave;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.InternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.filesave.event_structure.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.filesave.event_structure.FileSaveInternalData;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ProjectExternalEventContext;
import lombok.Setter;
import lombok.ToString;

@EventQualifier("file_save")
@Setter
@ToString
public class FileSaveEvent extends DeclarativeChainEvent<ProjectExternalEventContext> {

    private FileSaveExternalData externalData;

    private FileSaveInternalData internalData;




    // ПОЛЕ И ГЕТТЕР ДОЛЖНЫ БЫТЬ ИДЕНТИЧНЫ ПО НАЗВАНИЮ!

    @Override
    public InternalEventData getInternalData() {
        return internalData;
    }

    @Override
    public ExternalEventData getExternalData() {
        return externalData;
    }
}
