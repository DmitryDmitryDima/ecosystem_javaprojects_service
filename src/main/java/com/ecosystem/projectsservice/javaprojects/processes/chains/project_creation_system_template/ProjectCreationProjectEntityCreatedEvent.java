package com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template;

import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.UserExternalEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class ProjectCreationProjectEntityCreatedEvent extends ApplicationEvent {

    private UserExternalEventContext context;
    private ProjectCreationEventData data;
    private ProjectCreationPaths paths;
    private ProjectCreationUserPreference preference;




    public ProjectCreationProjectEntityCreatedEvent(Object source) {
        super(source);
    }
}
