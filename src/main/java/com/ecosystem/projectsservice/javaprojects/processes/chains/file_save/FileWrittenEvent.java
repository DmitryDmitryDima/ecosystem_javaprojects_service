package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save;

import com.ecosystem.projectsservice.javaprojects.processes.queue.UserEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class FileWrittenEvent extends ApplicationEvent {

    private UserEventContext context;
    private FileSaveEventData data;

    public FileWrittenEvent(Object source) {
        super(source);
    }
}
