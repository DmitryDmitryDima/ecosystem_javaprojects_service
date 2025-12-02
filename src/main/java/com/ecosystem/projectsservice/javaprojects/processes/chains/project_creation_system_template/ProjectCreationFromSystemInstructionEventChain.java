package com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.processes.queue.UserEventContext;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import com.ecosystem.projectsservice.javaprojects.service.ProjectConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ProjectCreationFromSystemInstructionEventChain {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private ProjectConstructor projectConstructor;


    @Autowired
    private ApplicationEventPublisher publisher;


    private static final String resultingEventName = "java_project_creation";


    public void initChain(SecurityContext securityContext, RequestContext requestContext, ProjectBuildFromTemplateInfo info){

        // пользовательский контекст - мигрирует по всей цепочке и по итогу уходит в очередь
        UserEventContext eventContext = UserEventContext.builder()
                .correlationId(requestContext.getCorrelationId())
                .timestamp(Instant.now())
                .username(securityContext.getUsername())
                .userUUID(securityContext.getUuid())
                .build();

        // event data

    }




    public void compensation(){

    }


    public void sendResult(){

    }






}
