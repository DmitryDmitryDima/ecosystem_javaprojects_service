package com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.directories;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectoryRemovalRequest {

    private UUID directoryId;
}
