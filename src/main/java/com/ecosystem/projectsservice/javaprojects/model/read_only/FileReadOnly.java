package com.ecosystem.projectsservice.javaprojects.model.read_only;


import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileReadOnly {
    private UUID parent_id;
    private String name;
    private UUID id;
    private String constructed_path;
    private Instant created_at;
    private Instant updated_at;
    private boolean hidden;
    private boolean immutable;
    private String extension;
    private FileStatus status;
    private Long version;
}
