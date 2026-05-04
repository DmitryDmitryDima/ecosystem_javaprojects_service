package com.ecosystem.projectsservice.javaprojects;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
public class S3Test {


    @Autowired
    private S3Client s3Client;


    @Test
    public void uploadStringFile(){


    }
}
