package com.ecosystem.projectsservice.javaprojects.model;


import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileReadOnly {
    private Long parent_id;
    private String name;
    private Long id;
    private String constructed_path;
    private Instant created_at;
    private Instant updated_at;
    private boolean hidden;
    private boolean immutable;
    private String extension;
    private FileStatus status;
}
