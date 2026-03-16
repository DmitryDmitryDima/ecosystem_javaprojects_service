package com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileMoveRequest {

    @NotNull
    private Long parentId; // директория, куда планируется переместить файл

    @NotNull
    private Long fileId; // файл


}
