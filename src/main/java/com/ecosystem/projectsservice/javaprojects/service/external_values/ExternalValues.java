package com.ecosystem.projectsservice.javaprojects.service.external_values;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


// собираем в одном месте все данные, загружаемые из properties
@Component
public class ExternalValues {



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


    @Value("${users.activity_events.exchange.name}")
    private String USERS_ACTIVITY_EXCHANGE_NAME;

    @Value("${users.projects_events.exchange.name}")
    private String USERS_PROJECTS_EVENTS_EXCHANGE_NAME;


    @Value("${system.projects_events.exchange.name}")
    private String SYSTEM_PROJECTS_EVENTS_EXCHANGE_NAME;


    public String getStorageEndpoint() {
        return storageEndpoint;
    }

    public void setStorageEndpoint(String storageEndpoint) {
        this.storageEndpoint = storageEndpoint;
    }

    public String getStorageUserBucket() {
        return storageUserBucket;
    }

    public void setStorageUserBucket(String storageUserBucket) {
        this.storageUserBucket = storageUserBucket;
    }

    public String getStorageSystemBucket() {
        return storageSystemBucket;
    }

    public void setStorageSystemBucket(String storageSystemBucket) {
        this.storageSystemBucket = storageSystemBucket;
    }

    public String getSystemStoragePath() {
        return systemStoragePath;
    }

    public String getUserStoragePath() {
        return userStoragePath;
    }

    public String getUsersActivityExchangeName() {
        return USERS_ACTIVITY_EXCHANGE_NAME;
    }

    public String getUsersProjectsEventsExchangeName() {
        return USERS_PROJECTS_EVENTS_EXCHANGE_NAME;
    }

    public String getSystemProjectsEventsExchangeName() {
        return SYSTEM_PROJECTS_EVENTS_EXCHANGE_NAME;
    }
}
