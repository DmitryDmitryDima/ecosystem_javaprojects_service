package com.ecosystem.projectsservice.javaprojects.processes;

import com.ecosystem.projectsservice.javaprojects.processes.external_queue.ProjectEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_queue.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
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

    @Value("${users.projects_events.exchange.name}")
    private String USERS_PROJECT_EVENTS_EXCHANGE_NAME;


    @EventListener
    @Async
    public void catchUserEvent(UserEvent event){
        //System.out.println(event+" catched");
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

    @EventListener
    @Async
    public void catchProjectUserEvent(ProjectEvent event){
        //System.out.println("project event "+event);
        try {
            MessagePostProcessor postProcessor = (message )->{
                message.getMessageProperties().setHeader("event_type", event.getEvent_type());
                return message;
            };

            String payload = mapper.writeValueAsString(event);

            rabbitTemplate.convertAndSend(USERS_PROJECT_EVENTS_EXCHANGE_NAME, "", payload, postProcessor);


        }
        catch (Exception e){

        }
    }







}
