package com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectRemovalRequest {
    private UUID projectId;

}
