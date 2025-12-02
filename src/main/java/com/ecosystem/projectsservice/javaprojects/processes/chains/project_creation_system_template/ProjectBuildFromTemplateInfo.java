package com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template;

import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectBuildFromTemplateInfo {

    // путь к инструкции
    private Path instructionsPath;

    // путь к шаблонам
    private Path fileTemplatesPath;

    // путь к проектам
    private Path projectsPath;

    private String projectName;

    // тип проекта
    private ProjectType projectType;


    private boolean needEntryPoint;


}
