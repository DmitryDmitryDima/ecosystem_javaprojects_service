package com.ecosystem.projectsservice.javaprojects.configuration;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Value("${users.activity_events.exchange.name}")
    private String USERS_ACTIVITY_EXCHANGE_NAME;

    @Value("${users.projects_events.exchange.name}")
    private String USERS_PROJECTS_EVENTS_EXCHANGE_NAME;

    @Value("${system.projects_events.exchange.name}")
    private String SYSTEM_PROJECTS_EVENTS_EXCHANGE_NAME;


    // узел, куда уходят персональные ивенты для пользователя
    @Bean
    public FanoutExchange usersActivityExchange(){
        return new FanoutExchange(USERS_ACTIVITY_EXCHANGE_NAME);
    }

    // узел, куда уходят ивенты системы, адресованные комнате проекта
    @Bean
    public FanoutExchange systemProjectsEventsExchange(){
        return new FanoutExchange(SYSTEM_PROJECTS_EVENTS_EXCHANGE_NAME);
    }

    // узел, куда уходят ивенты пользователей, адресованные комнате проекта
    @Bean
    public FanoutExchange usersProjectsEventsExchange(){
        return new FanoutExchange(USERS_PROJECTS_EVENTS_EXCHANGE_NAME);
    }
}
