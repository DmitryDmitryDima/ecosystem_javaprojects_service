package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectCreationRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectRemovalRequest;
import com.ecosystem.projectsservice.javaprojects.service.ProjectsService;
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
    public void testProjectCreation(){

        SecurityContext securityContext = SecurityContext.builder()
                .role("USER")
                .username("dima")
                .uuid(UUID.fromString("e9bccab3-93ce-4b83-bb3c-6d26e521c50c"))
                .build();

        ProjectCreationRequest request = new ProjectCreationRequest("test1", true, "maven", "");

        Assertions.assertDoesNotThrow(()->{
            projectsService.createProjectFromSystemTemplate(securityContext, request);
        });


    }


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


}
