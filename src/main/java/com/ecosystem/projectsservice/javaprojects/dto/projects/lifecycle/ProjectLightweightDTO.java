package com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle;


import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProjectLightweightDTO {

    private Long id;
    private ProjectStatus status;
    private String name;


}
