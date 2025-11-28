package com.ecosystem.projectsservice.javaprojects.configuration;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Value("${users.activity_events.exchange.name}")
    private String USERS_ACTIVITY_EXCHANGE_NAME;

    @Bean
    public FanoutExchange usersActivityExchange(){
        return new FanoutExchange(USERS_ACTIVITY_EXCHANGE_NAME);
    }
}
