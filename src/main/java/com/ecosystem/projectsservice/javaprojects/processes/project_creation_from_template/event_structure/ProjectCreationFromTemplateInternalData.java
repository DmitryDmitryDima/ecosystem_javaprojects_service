package com.ecosystem.projectsservice.javaprojects.processes.project_creation_from_template.event_structure;

import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.InternalEventData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectCreationFromTemplateInternalData extends InternalEventData {

    // путь к инструкции
    private String instructionsPath;

    // путь к шаблонам
    private String fileTemplatesPath;

    // путь к проектам
    private String projectsPath;



    // тип проекта
    private ProjectType projectType;


    private boolean needEntryPoint;
}
