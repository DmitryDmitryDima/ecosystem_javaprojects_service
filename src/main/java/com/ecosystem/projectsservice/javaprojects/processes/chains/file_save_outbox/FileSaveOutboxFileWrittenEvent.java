package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox;

import com.ecosystem.projectsservice.javaprojects.processes.chains.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.EventName;
import com.ecosystem.projectsservice.javaprojects.processes.chains.Retryable;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ProjectExternalEventContext;
import lombok.Getter;
import lombok.Setter;

@Retryable(count = 3)
@EventName("outbox_file_save_file_written")
@Getter
@Setter
public class FileSaveOutboxFileWrittenEvent extends ChainEvent {

    private ProjectExternalEventContext context;
    private FileSaveEventData data;

    public FileSaveOutboxFileWrittenEvent(){

    }
}
