package com.ecosystem.projectsservice.javaprojects.processes;

import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.ProjectEvent;
import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;


// присылаются
@Component
public class ExternalQueueBridge {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Autowired
    private InternalEventsManager manager;







    @Value("${users.activity_events.exchange.name}")
    private String USERS_ACTIVITY_EXCHANGE_NAME;

    @Value("${users.projects_events.exchange.name}")
    private String USERS_PROJECT_EVENTS_EXCHANGE_NAME;

    @PostConstruct
    public void registerQueueEvents(){


        manager.registerBridge(List.of(
                ProjectEvent.class,
                UserEvent.class
        ));
    }


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
