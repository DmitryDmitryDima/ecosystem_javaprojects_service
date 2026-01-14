package com.ecosystem.projectsservice.javaprojects.processes.chains.project_removal;


import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.UserExternalEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class ProjectRemovalInitiationEvent extends ApplicationEvent {

    private UserExternalEventContext context;
    private ProjectRemovalEventData data;
    private String projectsPath;

    public ProjectRemovalInitiationEvent(Object source, String projectsPath) {
        super(source);
        this.projectsPath = projectsPath;
    }
}
