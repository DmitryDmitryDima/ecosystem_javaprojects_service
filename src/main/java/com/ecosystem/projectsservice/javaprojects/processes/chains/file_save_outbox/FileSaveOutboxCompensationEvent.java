package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox;

import com.ecosystem.projectsservice.javaprojects.processes.chains.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.CompensationEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.EventName;
import com.ecosystem.projectsservice.javaprojects.processes.chains.Retryable;
import lombok.Getter;

@Getter
@Retryable(count = 3)
@EventName(value = "outbox_file_save_compensation")
public class FileSaveOutboxCompensationEvent extends CompensationEvent {

    private Long fileId;

    public FileSaveOutboxCompensationEvent(Long fileId) {
        this.fileId = fileId;
    }


}
