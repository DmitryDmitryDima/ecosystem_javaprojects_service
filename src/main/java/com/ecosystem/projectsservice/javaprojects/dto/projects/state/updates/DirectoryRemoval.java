package com.ecosystem.projectsservice.javaprojects.dto.projects.state.updates;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DirectoryRemoval {


    private List<UUID> files;



}
