package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FileSaveCompensationEvent extends ApplicationEvent {
    private long fileId;
    public FileSaveCompensationEvent(Object source, long fileId){
        super(source);
        this.fileId = fileId;

    }
}
