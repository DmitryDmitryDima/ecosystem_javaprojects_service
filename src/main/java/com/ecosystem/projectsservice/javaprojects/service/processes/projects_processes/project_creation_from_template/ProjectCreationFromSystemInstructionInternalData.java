package com.ecosystem.projectsservice.javaprojects.service.processes.projects_processes.project_creation_from_template;

import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectPrivacyLevel;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectType;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.InternalEventData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectCreationFromSystemInstructionInternalData
        extends InternalEventData {

    // тип проекта
    private ProjectType projectType;

    // нужна ли входная точка
    private boolean needEntryPoint;

    // уровень приватности
    private ProjectPrivacyLevel privacyLevel;

}
