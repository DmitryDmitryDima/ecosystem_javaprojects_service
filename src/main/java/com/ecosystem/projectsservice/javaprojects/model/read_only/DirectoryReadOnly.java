package com.ecosystem.projectsservice.javaprojects.model.read_only;

import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectoryReadOnly {

    private UUID parent_id;
    private String name;
    private UUID id;
    private String constructed_path;
    private Instant created_at;
    private boolean hidden;
    private boolean immutable;
    private DirectoryStatus status;
    private Long version;
    private Long depth;
}
