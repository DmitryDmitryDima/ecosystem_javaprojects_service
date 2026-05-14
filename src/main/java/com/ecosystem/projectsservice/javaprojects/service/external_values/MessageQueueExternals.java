package com.ecosystem.projectsservice.javaprojects.service.external_values;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageQueueExternals {



    @Value("${users.activity_events.exchange.name}")
    private String USERS_ACTIVITY_EXCHANGE_NAME;

    @Value("${users.projects_events.exchange.name}")
    private String USERS_PROJECTS_EVENTS_EXCHANGE_NAME;


    @Value("${system.projects_events.exchange.name}")
    private String SYSTEM_PROJECTS_EVENTS_EXCHANGE_NAME;




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
