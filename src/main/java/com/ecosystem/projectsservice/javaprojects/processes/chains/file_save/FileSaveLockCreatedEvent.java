package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save;


import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox.FileSaveEventData;
import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.ProjectExternalEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Setter
@Getter
public class FileSaveLockCreatedEvent extends ApplicationEvent {
    private String filePath;
    private ProjectExternalEventContext context;
    private FileSaveEventData data;

    public FileSaveLockCreatedEvent(Object source, String filePath) {
        super(source);
        this.filePath = filePath;
    }
}
