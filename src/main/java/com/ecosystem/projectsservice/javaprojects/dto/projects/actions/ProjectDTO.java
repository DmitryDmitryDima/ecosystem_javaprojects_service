package com.ecosystem.projectsservice.javaprojects.dto.projects.actions;

import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectStatus;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDTO {

    private String name;

    // при чтении в workspace может быть либо RUNNING, либо AVAILABLE
    private ProjectStatus status;

    private ProjectType projectType;

    private List<StructureMember> structure;

    private UUID author;




}
