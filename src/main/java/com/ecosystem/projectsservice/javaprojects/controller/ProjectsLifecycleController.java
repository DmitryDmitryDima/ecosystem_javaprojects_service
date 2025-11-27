package com.ecosystem.projectsservice.javaprojects.controller;


import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectCreationRequest;
import com.ecosystem.projectsservice.javaprojects.service.ProjectsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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






}
