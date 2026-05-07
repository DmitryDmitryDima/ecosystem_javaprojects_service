package com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileMoveRequest {

    @NotNull
    private UUID parentId; // директория, куда планируется переместить файл

    @NotNull
    private UUID fileId; // файл


}
