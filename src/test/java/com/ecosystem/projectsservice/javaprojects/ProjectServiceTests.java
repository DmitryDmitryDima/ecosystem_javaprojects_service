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













}
