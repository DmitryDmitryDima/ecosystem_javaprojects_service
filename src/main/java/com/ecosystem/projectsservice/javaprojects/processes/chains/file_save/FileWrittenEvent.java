package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save;

import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox.FileSaveEventData;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ProjectExternalEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class FileWrittenEvent extends ApplicationEvent {

    private ProjectExternalEventContext context;
    private FileSaveEventData data;

    public FileWrittenEvent(Object source) {
        super(source);
    }
}
