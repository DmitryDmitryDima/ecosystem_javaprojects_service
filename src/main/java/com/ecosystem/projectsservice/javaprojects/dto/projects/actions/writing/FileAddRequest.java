package com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileAddRequest {
    private Long parentId;
    private String filename;
    private String extension;
}
