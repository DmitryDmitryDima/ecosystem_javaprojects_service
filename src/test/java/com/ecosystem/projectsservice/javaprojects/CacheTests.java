package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.service.projects.access_validation.ProjectAccessValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
public class CacheTests {

    @Autowired
    private ProjectAccessValidator validator;

    @Test
    public void cacheTest(){
        validator.validateAccessUsingCache(null,
                null, UUID.randomUUID());


    }
}
