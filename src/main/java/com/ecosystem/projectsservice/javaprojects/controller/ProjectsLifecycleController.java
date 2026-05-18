package com.ecosystem.projectsservice.javaprojects.controller;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.*;
import com.ecosystem.projectsservice.javaprojects.service.projects.lifecycle.ProjectLifecycleService;
import com.ecosystem.projectsservice.javaprojects.service.projects.participants.ProjectParticipantsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

// создание и уничтожение java проекта
@RestController
@RequestMapping("/")
public class ProjectsLifecycleController {


    @Autowired
    private ProjectLifecycleService projectLifecycleService;

    @Autowired
    private ProjectParticipantsService participantsService;

    @PostMapping("/createProject")
    public ResponseEntity<Void> createProject(@RequestHeader Map<String, String> headers,
                                              @RequestBody ProjectCreationRequest request) throws Exception {

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

        return ResponseEntity.ok(participantsService.createInviteToken(securityContext, requestContext, request));
    }

    // валидация токена приглашения
    @PostMapping("/validateInviteToken/{token}")
    public ResponseEntity<InviteTokenValidationResponse> validateInviteToken(@RequestHeader Map<String, String> headers,
                                                                             @PathVariable("token") UUID token) throws Exception {

        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        return ResponseEntity.ok(participantsService
                .validateInviteToken(securityContext, requestContext, token));

    }

    // точечное добавление пользователя к проекту
    @PostMapping("/addParticipant")
    public ResponseEntity<Void> addParticipant(@RequestHeader Map<String, String> headers,
                                               @RequestBody ProjectAddParticipantRequest request)
    throws Exception
    {
        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);
        participantsService.addParticipantToProject(securityContext, requestContext, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/removeParticipant")
    public ResponseEntity<Void> removeParticipant(@RequestHeader Map<String, String> headers,
                                                  @RequestBody ProjectRemoveParticipantRequest request)
    throws Exception
    {
        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);
        participantsService.removeParticipantFromProject(securityContext, requestContext, request);
        return ResponseEntity.noContent().build();
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
