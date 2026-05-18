package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.service.projects.state.read.SnapshotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
public class ProjectServiceTests {

    @Autowired
    private SnapshotService snapshotService;



    @Test
    public void snap(){

        UUID fileId = UUID.fromString("019e2573-74d9-747e-9dcc-8b2557b67a5a");

        UUID projectId = UUID.fromString("019e2573-7012-7cc9-8915-e2be64de37f0");


        System.out.println(snapshotService.getProjectFile(projectId, fileId));

    }



















}
