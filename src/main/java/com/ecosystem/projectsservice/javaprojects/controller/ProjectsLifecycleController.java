package com.ecosystem.projectsservice.javaprojects.controller;


import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectCreationRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectRemovalRequest;
import com.ecosystem.projectsservice.javaprojects.service.ProjectsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// создание и уничтожение java проекта
@RestController
@RequestMapping("/")
public class ProjectsLifecycleController {


    @Autowired
    private ProjectsService projectsService;

    @PostMapping("/createProject")
    public ResponseEntity<Void> createProject(@Header Map<String, String> headers, ProjectCreationRequest request) throws Exception {

        SecurityContext securityContext = SecurityContext.generateContext(headers);

        // todo доп защиту стоит реализовать в фильтре, проверяющем, не пришел ли post запрос с неправильной ролью

        projectsService.createProjectFromSystemTemplate(securityContext, request);

        return ResponseEntity.noContent().build();


    }


    @PostMapping("/deleteProject")
    public ResponseEntity<Void> deleteProject(@Header Map<String, String> headers, ProjectRemovalRequest request)  {
        SecurityContext context = SecurityContext.generateContext(headers);
        projectsService.deleteProject(context, request);
        return ResponseEntity.noContent().build();
    }

    /*
    Возвращаем проекты пользователя. Тут в будущем нужно проверять права доступа - кому этот проект будет виден
     */
    @GetMapping("/getProjects/{targetUUID}")
    public ResponseEntity<List<ProjectDTO>> getAllProjects(@Header Map<String, String> headers,
                                                           @PathVariable("targetUUID") String targetUUID){
        SecurityContext context = SecurityContext.generateContext(headers);

        List<ProjectDTO> projects = projectsService.getAllProjects(context, UUID.fromString(targetUUID));
        return ResponseEntity.ok(projects);


    }









}
