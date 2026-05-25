package com.ecosystem.projectsservice.javaprojects.dto.projects.state.updates;


import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DirectoryMove {

    private UUID correlationId;

    private UUID userId;
    private String username;

    private List<FileDTO> touchedFiles;

}
