package com.ecosystem.projectsservice.javaprojects.processes;

import com.ecosystem.projectsservice.javaprojects.processes.queue.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


// присылаются
@Component
public class InternalEventsListener {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;



    @Value("${users.activity_events.exchange.name}")
    private String USERS_ACTIVITY_EXCHANGE_NAME;


    @EventListener
    public void catchUserEvent(UserEvent event){
        System.out.println(event+" catched");
        try {
            MessagePostProcessor postProcessor = (message )->{
                message.getMessageProperties().setHeader("event_type", event.getEvent_type());
                return message;
            };

            String payload = mapper.writeValueAsString(event);

            rabbitTemplate.convertAndSend(USERS_ACTIVITY_EXCHANGE_NAME, "", payload, postProcessor);


        }
        catch (Exception e){

        }
    }






}
