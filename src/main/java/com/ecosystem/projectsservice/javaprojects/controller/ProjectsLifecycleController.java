package com.ecosystem.projectsservice.javaprojects.controller;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.*;
import com.ecosystem.projectsservice.javaprojects.service.projects.ProjectLifecycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// создание и уничтожение java проекта
@RestController
@RequestMapping("/")
public class ProjectsLifecycleController {


    @Autowired
    private ProjectLifecycleService projectLifecycleService;

    @PostMapping("/createProject")
    public ResponseEntity<Void> createProject(@RequestHeader Map<String, String> headers, @RequestBody ProjectCreationRequest request) throws Exception {

        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);
        System.out.println(request);


        // todo доп защиту стоит реализовать в фильтре, проверяющем, не пришел ли post запрос с неправильной ролью

        projectLifecycleService.createProject(securityContext, requestContext, request);

        return ResponseEntity.noContent().build();


    }

    // удаление проекта
    @PostMapping("/deleteProject")
    public ResponseEntity<Void> deleteProject(@RequestHeader Map<String, String> headers, @RequestBody ProjectRemovalRequest request) throws Exception {
        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);
        projectLifecycleService.deleteProject(securityContext, requestContext, request);
        return ResponseEntity.noContent().build();
    }

    // создание пригласительного токена - его может создать только автор проекта
    @PostMapping("/createInviteToken")
    public ResponseEntity<UUID> createInviteToken(@RequestHeader Map<String, String> headers,
                                                  @RequestBody ProjectInviteCreationRequest request) throws Exception{

        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        return ResponseEntity.ok(projectLifecycleService.createInviteToken(securityContext, requestContext, request));
    }

    /*
    Возвращаем проекты пользователя. Тут в будущем нужно проверять права доступа - кому этот проект будет виден
     */
    @GetMapping("/getProjects")
    public ResponseEntity<AllTargetRelatedProjects> getAllProjects(@RequestHeader Map<String, String> headers,
                                                                         @RequestParam("targetUsername") String targetUsername){
        SecurityContext context = SecurityContext.generateContext(headers);


        return ResponseEntity.ok(projectLifecycleService.getAllProjects(context, targetUsername));


    }









}
