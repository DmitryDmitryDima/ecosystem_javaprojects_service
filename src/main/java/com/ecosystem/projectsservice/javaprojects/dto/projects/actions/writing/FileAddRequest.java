package com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileAddRequest {
    @NotNull
    private Long parentId;

    @NotNull
    private String filename;

    @NotNull
    private String extension;
}
