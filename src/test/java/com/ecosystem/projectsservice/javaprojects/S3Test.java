package com.ecosystem.projectsservice.javaprojects;

import com.ecosystem.projectsservice.javaprojects.service.external_values.ExternalValues;
import com.ecosystem.projectsservice.javaprojects.service.external_values.StorageExternals;
import com.ecosystem.projectsservice.javaprojects.service.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class S3Test {


    @Autowired
    private StorageService storageService;

    @Autowired
    private StorageExternals storageExternals;


    @Test
    public void testDownload(){


        storageService.delete(storageExternals
                        .getStorageSystemBucket(),
                "standart_pom.txt");



    }
}
