package com.ecosystem.projectsservice.javaprojects.dto.projects.state.updates;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CachedFileInvalidation {

    private UUID fileId;
}
