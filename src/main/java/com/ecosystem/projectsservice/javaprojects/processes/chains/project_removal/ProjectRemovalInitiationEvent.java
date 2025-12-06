package com.ecosystem.projectsservice.javaprojects.processes.chains.project_removal;


import com.ecosystem.projectsservice.javaprojects.processes.queue.UserEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class ProjectRemovalInitiationEvent extends ApplicationEvent {

    private UserEventContext context;
    private ProjectRemovalEventData data;

    public ProjectRemovalInitiationEvent(Object source) {
        super(source);
    }
}
