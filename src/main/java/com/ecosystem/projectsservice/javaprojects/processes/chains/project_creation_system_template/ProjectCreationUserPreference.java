package com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template;

import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// предпочтения юзера относительно проекта
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectCreationUserPreference {

    // тип проекта
    private ProjectType projectType;


    private boolean needEntryPoint;
}
