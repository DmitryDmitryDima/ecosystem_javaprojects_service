package com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileDTO {
    private String content;
    private String extension;
    private String name;
    private String constructedPath;
    private Long id;

    // кешируем uuid владельца файла
    private UUID ownerUUID;

    private Long projectId;


}
