package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save;

import com.ecosystem.projectsservice.javaprojects.processes.queue.ProjectEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.queue.UserEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class FileSaveInitiationEvent extends ApplicationEvent {

    private String projectsPath;

    private ProjectEventContext context;
    private FileSaveEventData data;

    public FileSaveInitiationEvent(Object source, String projectsPath) {
        super(source);
        this.projectsPath = projectsPath;
    }
}
