package com.ecosystem.projectsservice.javaprojects.processes.events.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Service;

@Getter
@Setter
public class ProjectRemovalCleanedDiskEvent extends ApplicationEvent {
    private Long projectId;

    public ProjectRemovalCleanedDiskEvent(Object source, Long projectId) {
        super(source);
        this.projectId = projectId;
    }
}
