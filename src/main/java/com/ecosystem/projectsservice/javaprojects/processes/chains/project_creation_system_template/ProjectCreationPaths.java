package com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectCreationPaths {
    private String instructionsPath;

    // путь к шаблонам
    private String fileTemplatesPath;

    // путь к проектам
    private String projectsPath;
}
