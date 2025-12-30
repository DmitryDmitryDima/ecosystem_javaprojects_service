package com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template;

import com.ecosystem.projectsservice.javaprojects.processes.external_queue.UserEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

// ивент, выбрасываемый методом init
@Getter
@Setter
public class ProjectCreationInitiationEvent extends ApplicationEvent {
    private UserEventContext context;
    private ProjectCreationEventData data;
    private ProjectCreationPaths paths;
    private ProjectCreationUserPreference preference;

    public ProjectCreationInitiationEvent(Object source) {
        super(source);
    }
}
