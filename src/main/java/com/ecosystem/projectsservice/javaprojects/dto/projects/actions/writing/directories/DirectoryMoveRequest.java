package com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.directories;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DirectoryMoveRequest {
    @NotNull
    private UUID directoryId;

    @NotNull
    private UUID parentId;
}
