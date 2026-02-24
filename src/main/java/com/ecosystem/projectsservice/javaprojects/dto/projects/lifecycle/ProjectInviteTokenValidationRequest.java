package com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectInviteTokenValidationRequest {
    @NotNull
    private UUID token;
}
