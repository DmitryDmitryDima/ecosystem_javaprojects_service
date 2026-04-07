package com.ecosystem.projectsservice.javaprojects.service.projects.access_validation;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.ProjectParticipant;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectStatus;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectDatabaseValidator {

    @Autowired
    private ProjectRepository projectRepository;

    @Transactional
    public ProjectDTO validate(SecurityContext securityContext,
                               RequestContext requestContext,
                               UUID projectId){

        Optional<Project> projectCheck = projectRepository.findById(projectId);

        if (projectCheck.isEmpty() || projectCheck.get().getStatus()== ProjectStatus.REMOVING) throw new IllegalStateException("Проекта не существует");

        Project project = projectCheck.get();

        // действие выполняется хозяином проекта
        if (project.getUserUUID().equals(securityContext.getUuid())) return prepareDTO(project);


        boolean existed = false;
        List<ProjectParticipant> participants = project.getParticipants(); // извлекаем один раз для lazy транзакции
        for (ProjectParticipant participant:participants){
            if (participant.getUserUUID().equals(securityContext.getUuid())){
                existed = true;
                break;
            }
        }
        if (!existed){
            throw new IllegalStateException("Пользователь не является участником проекта");

        }





        return prepareDTO(project);

    }

    private ProjectDTO prepareDTO(Project project){
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setRoot(project.getRoot().getId());
        projectDTO.setProjectType(project.getType());
        projectDTO.setStatus(project.getStatus());
        projectDTO.setName(project.getName());
        projectDTO.setAuthor(project.getUserUUID());
        projectDTO.setParticipants(project.getParticipants().stream().map(ProjectParticipant::getUserUUID).toList());
        projectDTO.setId(project.getId());
        projectDTO.setEntryPoint(project.getEntryPoint().getId());
        return projectDTO;
    }


}
