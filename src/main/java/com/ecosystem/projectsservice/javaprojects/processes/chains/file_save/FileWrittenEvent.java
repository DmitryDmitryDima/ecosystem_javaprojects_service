package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save;

import com.ecosystem.projectsservice.javaprojects.processes.external_queue.ProjectEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class FileWrittenEvent extends ApplicationEvent {

    private ProjectEventContext context;
    private FileSaveEventData data;

    public FileWrittenEvent(Object source) {
        super(source);
    }
}
