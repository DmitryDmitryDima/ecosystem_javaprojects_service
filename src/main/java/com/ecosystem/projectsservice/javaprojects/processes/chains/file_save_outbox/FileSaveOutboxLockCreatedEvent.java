package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox;

import com.ecosystem.projectsservice.javaprojects.processes.chains.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.EventName;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ProjectExternalEventContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EventName("outbox_file_save_lock_created")
public class FileSaveOutboxLockCreatedEvent extends ChainEvent {

    private String filePath;

    private ProjectExternalEventContext context;
    private FileSaveEventData data;

    public FileSaveOutboxLockCreatedEvent(String filePath) {

        this.filePath = filePath;
    }
}
