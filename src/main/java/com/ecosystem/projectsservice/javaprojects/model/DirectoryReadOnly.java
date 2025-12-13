package com.ecosystem.projectsservice.javaprojects.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectoryReadOnly {

    private Long parent_id;
    private String name;
    private Long id;
    private String constructed_path;
    private Instant created_at;
    private boolean hidden;
    private boolean immutable;
}
