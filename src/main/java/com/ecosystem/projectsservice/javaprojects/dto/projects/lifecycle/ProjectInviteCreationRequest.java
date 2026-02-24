package com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectInviteCreationRequest {
    @NotNull
    private UUID projectId;
    // если инвайт персональный
    private UUID userUUID;

}
