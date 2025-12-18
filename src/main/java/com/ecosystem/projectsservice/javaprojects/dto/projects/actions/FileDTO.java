package com.ecosystem.projectsservice.javaprojects.dto.projects.actions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDTO {
    private String content;
    private String extension;
    private String constructedPath;
    private Instant lastUpdate;
}
