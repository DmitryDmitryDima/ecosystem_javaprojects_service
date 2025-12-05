package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectCreationRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectRemovalRequest;
import com.ecosystem.projectsservice.javaprojects.service.ProjectsService;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
public class ProjectServiceTests {

    @Autowired
    private ProjectsService projectsService;




    @Test
    public void testProjectRemoval(){
        SecurityContext securityContext = SecurityContext.builder()
                .role("USER")
                .username("user_1")
                .uuid(UUID.fromString("704eec39-e68a-4aee-a783-c47935a9de03"))
                .build();
        ProjectRemovalRequest request = new ProjectRemovalRequest(5L);

        projectsService.deleteProject(securityContext, request);
    }

    @Test
    public void testProjectCreationInternal(){
        SecurityContext securityContext = SecurityContext.builder()
                .role("USER")
                .username("user_1")
                .uuid(UUID.fromString("b7a3cf50-c9d8-4f9e-95e8-5a8fb505edc5"))
                .build();

        RequestContext requestContext = RequestContext.builder().correlationId(UUID.randomUUID()).build();

        ProjectCreationRequest request = new ProjectCreationRequest();
        request.setProjectType("maven");
        request.setName("my_project");
        request.setNeedEntryPoint(true);

        projectsService.createProject(securityContext, requestContext, request);




    }


}
