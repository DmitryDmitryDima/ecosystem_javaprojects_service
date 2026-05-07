package com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.directories;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectoryAddRequest {

    @NotNull
    private UUID parentId;

    @NotNull
    private String name;

}
