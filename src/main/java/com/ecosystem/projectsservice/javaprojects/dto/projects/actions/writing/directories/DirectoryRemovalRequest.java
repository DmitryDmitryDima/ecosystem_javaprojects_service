package com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.directories;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectoryRemovalRequest {

    private Long directoryId;
}
