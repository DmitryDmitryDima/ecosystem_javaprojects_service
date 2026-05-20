package com.ecosystem.projectsservice.javaprojects.dto.projects.state;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FileMove {


    private FileDTO fileDTO;

    private UUID correlationId;

    private UUID userId;
    private String username;


}
