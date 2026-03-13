package com.ecosystem.projectsservice.javaprojects.service.projects;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.ProjectParticipant;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectStatus;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


// проверяет, имеет ли пользователь доступ к проекту и может ли совершать в нем действия
// todo короткоживущие токены доступа
@Service
public class ProjectAccessValidator {

    @Autowired
    private ProjectRepository projectRepository;



    public Project validateAccess(SecurityContext securityContext, RequestContext requestContext, UUID projectId){
        Optional<Project> projectCheck = projectRepository.findById(projectId);

        if (projectCheck.isEmpty() || projectCheck.get().getStatus()== ProjectStatus.REMOVING) throw new IllegalStateException("Проекта не существует");

        Project project = projectCheck.get();

        // действие выполняется хозяином проекта
        if (project.getUserUUID().equals(securityContext.getUuid())) return project;


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



        // todo проверка доступа к проекту

        return project;
    }
}
