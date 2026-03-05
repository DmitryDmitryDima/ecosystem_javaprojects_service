package com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectRemoveParticipantRequest {

    private UUID projectId;
    private UUID userId;
    private String username;

}
