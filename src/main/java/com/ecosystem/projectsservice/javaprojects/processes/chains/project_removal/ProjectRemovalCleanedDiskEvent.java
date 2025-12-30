package com.ecosystem.projectsservice.javaprojects.processes.chains.project_removal;

import com.ecosystem.projectsservice.javaprojects.processes.external_queue.UserEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class ProjectRemovalCleanedDiskEvent extends ApplicationEvent {

    private UserEventContext context;
    private ProjectRemovalEventData eventData;

    public ProjectRemovalCleanedDiskEvent(Object source) {
        super(source);

    }
}
