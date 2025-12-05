package com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template;

import com.ecosystem.projectsservice.javaprojects.processes.queue.UserEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;


@Getter
@Setter
public class ProjectCreationRootWrittenEvent extends ApplicationEvent {

    private ProjectCreationPaths paths;
    private UserEventContext context;
    private ProjectCreationEventData data;
    private ProjectCreationUserPreference preference;

    public ProjectCreationRootWrittenEvent(Object source) {
        super(source);
    }
}
