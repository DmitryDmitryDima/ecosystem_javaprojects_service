package com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleFileInfo {
    private String name;
    private UUID id;
    private String path;
    private String extension;
}
