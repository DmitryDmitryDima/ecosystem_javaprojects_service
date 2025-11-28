package com.ecosystem.projectsservice.javaprojects.processes.events.entitiesflow;

import com.ecosystem.projectsservice.javaprojects.processes.events.UserEventContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class ProjectRemovalCleanedDiskEvent extends ApplicationEvent {
    private Long projectId;
    private UserEventContext context;

    public ProjectRemovalCleanedDiskEvent(Object source, Long projectId) {
        super(source);
        this.projectId = projectId;
    }
}
