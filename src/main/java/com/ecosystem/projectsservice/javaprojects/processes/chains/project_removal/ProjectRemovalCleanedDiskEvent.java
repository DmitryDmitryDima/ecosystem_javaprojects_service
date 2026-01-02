package com.ecosystem.projectsservice.javaprojects.processes.chains.project_removal;

import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.UserExternalEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class ProjectRemovalCleanedDiskEvent extends ApplicationEvent {

    private UserExternalEventContext context;
    private ProjectRemovalEventData eventData;

    public ProjectRemovalCleanedDiskEvent(Object source) {
        super(source);

    }
}
