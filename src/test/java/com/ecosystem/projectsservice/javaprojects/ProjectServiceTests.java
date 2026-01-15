package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.ProjectCreationRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.ProjectRemovalRequest;
import com.ecosystem.projectsservice.javaprojects.service.projects.ProjectLifecycleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
public class ProjectServiceTests {

    @Autowired
    private ProjectLifecycleService projectLifecycleService;




    @Test
    public void testProjectRemoval() throws Exception {
        SecurityContext securityContext = SecurityContext.builder()
                .role("USER")
                .username("dima")
                .uuid(UUID.fromString("e9bccab3-93ce-4b83-bb3c-6d26e521c50c"))
                .build();
        ProjectRemovalRequest request = new ProjectRemovalRequest(52L);


        RequestContext requestContext = RequestContext.builder().correlationId(UUID.randomUUID()).build();

        projectLifecycleService.deleteProject(securityContext, requestContext, request);
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

        try {
            projectLifecycleService.createProject(securityContext, requestContext, request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }




}
