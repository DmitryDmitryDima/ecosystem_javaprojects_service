package com.ecosystem.projectsservice.javaprojects;

import com.ecosystem.projectsservice.javaprojects.service.external_values.ExternalValues;
import com.ecosystem.projectsservice.javaprojects.service.external_values.StorageExternals;
import com.ecosystem.projectsservice.javaprojects.service.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
public class S3Test {


    @Autowired
    private StorageService storageService;

    @Autowired
    private StorageExternals storageExternals;


    @Test
    public void testLoad(){


        storageService.saveOrUpdate(storageExternals.getStorageUserBucket(),
                UUID.randomUUID().toString(), "hello");



    }
}
