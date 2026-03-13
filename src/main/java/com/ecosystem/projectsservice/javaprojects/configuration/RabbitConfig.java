package com.ecosystem.projectsservice.javaprojects.configuration;

import com.ecosystem.projectsservice.javaprojects.service.ExternalValues;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {


    @Autowired
    private ExternalValues externalValues;





    // узел, куда уходят персональные ивенты для пользователя
    @Bean
    public FanoutExchange usersActivityExchange(){
        return new FanoutExchange(externalValues.getUsersActivityExchangeName());
    }

    // узел, куда уходят ивенты системы, адресованные комнате проекта
    @Bean
    public FanoutExchange systemProjectsEventsExchange(){
        return new FanoutExchange(externalValues.getSystemProjectsEventsExchangeName());
    }

    // узел, куда уходят ивенты пользователей, адресованные комнате проекта
    @Bean
    public FanoutExchange usersProjectsEventsExchange(){
        return new FanoutExchange(externalValues.getUsersProjectsEventsExchangeName());
    }
}
