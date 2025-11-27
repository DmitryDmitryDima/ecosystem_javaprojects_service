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
                .username("user_1")
                .uuid(UUID.randomUUID())
                .build();

        ProjectCreationRequest request = new ProjectCreationRequest("test", true, "maven");

        Assertions.assertDoesNotThrow(()->{
            projectsService.createProjectFromSystemTemplate(securityContext, request);
        });


    }


    @Test
    public void testProjectRemoval(){
        SecurityContext securityContext = SecurityContext.builder()
                .role("USER")
                .username("user_1")
                .uuid(UUID.fromString("0a05927c-ca3f-40dc-8658-9121af4617c2"))
                .build();
        ProjectRemovalRequest request = new ProjectRemovalRequest(1L);

        projectsService.deleteProject(securityContext, request);
    }
}
