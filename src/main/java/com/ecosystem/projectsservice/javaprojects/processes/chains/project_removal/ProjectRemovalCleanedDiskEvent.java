package com.ecosystem.projectsservice.javaprojects.processes.chains.project_removal;

import com.ecosystem.projectsservice.javaprojects.processes.queue.UserEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class ProjectRemovalCleanedDiskEvent extends ApplicationEvent {
    private Long projectId;
    private UserEventContext context;
    private ProjectRemovalEventData metadata;

    public ProjectRemovalCleanedDiskEvent(Object source) {
        super(source);

    }
}
