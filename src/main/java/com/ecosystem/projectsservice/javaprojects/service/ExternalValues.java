package com.ecosystem.projectsservice.javaprojects.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


// собираем в одном месте все данные, загружаемые из properties
@Component
public class ExternalValues {


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
