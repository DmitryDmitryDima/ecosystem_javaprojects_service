package com.ecosystem.projectsservice.javaprojects.service.projects.access_validation;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.cache.ProjectValidationHash;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import com.ecosystem.projectsservice.javaprojects.repository.cache.ProjectValidationHashRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;


// проверяет, имеет ли пользователь доступ к проекту и может ли совершать в нем действия
// полагается либо на кеш, либо на бд
@Service
public class ProjectAccessValidator {

    // источник истины
    @Autowired
    private ProjectDatabaseValidator projectDatabaseValidator;





    @Autowired
    private ProjectValidationHashRepository cacheValidationRepo;










    // прямой запрос к db
    public ProjectDTO validateAccessUsingDb(SecurityContext securityContext,
                                            RequestContext requestContext,
                                            UUID projectId){


        return projectDatabaseValidator.validate(securityContext, requestContext, projectId);
    }

    // метод, предполагающий обращение к кешу и кеширование некоторых параметров проекта
    public ProjectValidationHash validateAccessUsingCache(SecurityContext securityContext,
                                                         RequestContext requestContext,
                                                         UUID projectId){






        String key = securityContext.getUuid()+":"+projectId;

        // проверяем, существует ли кеш валидации

        Optional<ProjectValidationHash> check = cacheValidationRepo.findById(key);

        // записи нет, нужно провести db валидацию
        if (check.isEmpty()){

            System.out.println("DATABASE VALIDATION");
            ProjectDTO project = projectDatabaseValidator.validate(securityContext,
                    requestContext,
                    projectId);
            ProjectValidationHash hash = new ProjectValidationHash();
            hash.setId(key);
            hash.setProjectOwner(project.getAuthor());
            hash.setRoot(project.getRoot());
            // кешируем
            cacheValidationRepo.save(hash);
            return hash;
        }
        else {
            System.out.println("CACHE VALIDATION");
            return check.get();
        }





    }
}
