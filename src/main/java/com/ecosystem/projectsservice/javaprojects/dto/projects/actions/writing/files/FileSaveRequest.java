package com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileSaveRequest {


    private String content;
    @NotNull
    private UUID fileId;


}
