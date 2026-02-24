package com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/*
при чтении для ясности мы явно разделяем авторские проекты пользователя (target) и проекты, где он является участником
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AllTargetRelatedProjects {



    private List<ProjectLightweightDTO> authorProjects;
    private List<ProjectLightweightDTO> participantProjects;

}
