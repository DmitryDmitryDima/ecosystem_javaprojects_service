package com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template;

import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectBuildFromTemplateInfo {

    // путь к инструкции
    private String instructionsPath;

    // путь к шаблонам
    private String fileTemplatesPath;

    // путь к проектам
    private String projectsPath;

    private String projectName;

    // тип проекта
    private ProjectType projectType;


    private boolean needEntryPoint;


}
