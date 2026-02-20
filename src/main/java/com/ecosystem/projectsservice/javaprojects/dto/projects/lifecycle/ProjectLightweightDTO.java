package com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle;


import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectPrivacyLevel;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProjectLightweightDTO {

    private Long id;
    private ProjectStatus status;
    private String name;
    private UUID author;
    private ProjectPrivacyLevel privacyLevel;


}
