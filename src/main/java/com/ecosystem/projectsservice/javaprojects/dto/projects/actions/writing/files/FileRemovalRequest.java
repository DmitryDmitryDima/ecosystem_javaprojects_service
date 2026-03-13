package com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileRemovalRequest {
    @NotNull
    private Long fileId;
}
