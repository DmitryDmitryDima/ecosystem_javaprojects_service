package com.ecosystem.projectsservice.javaprojects.dto.projects.state;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Autosave {

    private Long fileId;
    private UUID projectId;


}
