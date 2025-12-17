package com.ecosystem.projectsservice.javaprojects.dto.projects.actions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleFileView {
    private String name;
    private Long id;
    private String path;
}
