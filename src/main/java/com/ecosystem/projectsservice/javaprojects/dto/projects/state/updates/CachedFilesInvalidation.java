package com.ecosystem.projectsservice.javaprojects.dto.projects.state.updates;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CachedFilesInvalidation {

    private List<UUID> keys;
}
