package com.ecosystem.projectsservice.javaprojects.service.external_values;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class StorageExternals {


    @Value("${s3.endpoint}")
    private String storageEndpoint;

    @Value("${s3.user.bucket")
    private String storageUserBucket;

    @Value("${s3.system.bucket}")
    private String storageSystemBucket;


    @Value("${storage.system}")
    private String systemStoragePath;

    @Value("${storage.user}")
    private String userStoragePath;



}
