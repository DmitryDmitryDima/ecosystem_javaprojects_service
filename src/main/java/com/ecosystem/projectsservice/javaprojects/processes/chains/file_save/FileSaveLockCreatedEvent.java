package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save;


import com.ecosystem.projectsservice.javaprojects.processes.queue.ProjectEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.queue.UserEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Setter
@Getter
public class FileSaveLockCreatedEvent extends ApplicationEvent {
    private String filePath;
    private ProjectEventContext context;
    private FileSaveEventData data;

    public FileSaveLockCreatedEvent(Object source, String filePath) {
        super(source);
        this.filePath = filePath;
    }
}
