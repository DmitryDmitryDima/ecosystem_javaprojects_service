package com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.UserExternalEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

// ивент, выбрасываемый методом init
@Getter
@Setter
public class ProjectCreationInitiationEvent extends ApplicationEvent {
    private UserExternalEventContext context;
    private ProjectCreationEventData data;
    private ProjectCreationPaths paths;
    private ProjectCreationUserPreference preference;

    public ProjectCreationInitiationEvent(Object source) {
        super(source);
    }
}
