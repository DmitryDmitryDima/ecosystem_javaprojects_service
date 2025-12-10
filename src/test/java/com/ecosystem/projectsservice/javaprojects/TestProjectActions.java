package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import com.ecosystem.projectsservice.javaprojects.service.ProjectActionsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestProjectActions {

    @Autowired
    private ProjectActionsService service;


    @Test
    public void testReading(){
        long id = 83;
        try {
            service.readProject(null,null,id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
