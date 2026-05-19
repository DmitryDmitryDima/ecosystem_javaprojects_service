package com.ecosystem.projectsservice.javaprojects.dto.projects.state;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectStructureInvalidation {

    private UUID projectId;
}
