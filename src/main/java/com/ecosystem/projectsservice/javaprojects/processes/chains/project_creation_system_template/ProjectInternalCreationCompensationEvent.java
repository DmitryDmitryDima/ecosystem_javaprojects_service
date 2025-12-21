package com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;

@Getter
public class ProjectInternalCreationCompensationEvent extends ApplicationEvent {

    private Long projectId;
    private Path projectPath;

    public ProjectInternalCreationCompensationEvent(Object source, Long projectId, Path projectPath) {
        super(source);
        this.projectId = projectId;
        this.projectPath = projectPath;
    }
}
