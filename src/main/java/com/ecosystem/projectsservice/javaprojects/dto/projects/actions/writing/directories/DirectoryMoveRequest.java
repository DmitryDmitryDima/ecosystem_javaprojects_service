package com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.directories;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DirectoryMoveRequest {
    @NotNull
    private Long directoryId;

    @NotNull
    private Long parentId;
}
