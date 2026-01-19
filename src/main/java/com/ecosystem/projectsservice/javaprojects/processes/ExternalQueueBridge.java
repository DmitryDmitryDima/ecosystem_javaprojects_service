package com.ecosystem.projectsservice.javaprojects.processes;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.ProjectEventFromSystem;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.UserPersonalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.ChainManager;
import com.ecosystem.projectsservice.javaprojects.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;


// присылаются
@Component
public class ExternalQueueBridge {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;



    @Autowired
    private ChainManager chainManager;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;







    @Value("${users.activity_events.exchange.name}")
    private String USERS_ACTIVITY_EXCHANGE_NAME;

    @Value("${users.projects_events.exchange.name}")
    private String USERS_PROJECT_EVENTS_EXCHANGE_NAME;


    @Value("${system.projects_events.exchange.name}")
    private String SYSTEM_PROJECTS_EVENTS_EXCHANGE_NAME;


    // точка регистрации категорий

    @PostConstruct
    public void registerQueueEvents(){

        chainManager.registerExternalEvents(List.of(
                ProjectEventFromUser.class, UserPersonalEvent.class, ProjectEventFromSystem.class
        ));
    }


    @EventListener
    @Async
    public void catchProjectSystemEvent(ProjectEventFromSystem projectEventFromSystem){
        try {
            MessagePostProcessor postProcessor = (message )->{
                message.getMessageProperties().setHeader("event_type", projectEventFromSystem.getType());
                return message;
            };

            String payload = mapper.writeValueAsString(projectEventFromSystem);

            rabbitTemplate.convertAndSend(SYSTEM_PROJECTS_EVENTS_EXCHANGE_NAME, "", payload, postProcessor);


        }
        catch (Exception e){
            e.printStackTrace();
        }

        if (projectEventFromSystem.getOutboxParent()!=null){
            outboxCallback(projectEventFromSystem.getOutboxParent());
        }
    }

    @EventListener
    @Async
    public void catchUserActivityEvent(UserPersonalEvent event){
        System.out.println("user event ");
        try {
            MessagePostProcessor postProcessor = (message )->{
                message.getMessageProperties().setHeader("event_type", event.getType());
                return message;
            };

            String payload = mapper.writeValueAsString(event);

            rabbitTemplate.convertAndSend(USERS_ACTIVITY_EXCHANGE_NAME, "", payload, postProcessor);


        }
        catch (Exception e){
            e.printStackTrace();
        }

        if (event.getOutboxParent()!=null){
            outboxCallback(event.getOutboxParent());
        }
    }



    @EventListener
    @Async
    public void catchProjectUserEvent(ProjectEventFromUser event){
        System.out.println("project event "+event);
        try {
            MessagePostProcessor postProcessor = (message )->{
                message.getMessageProperties().setHeader("event_type", event.getType());
                return message;
            };

            String payload = mapper.writeValueAsString(event);

            rabbitTemplate.convertAndSend(USERS_PROJECT_EVENTS_EXCHANGE_NAME, "", payload, postProcessor);


        }
        catch (Exception e){
            e.printStackTrace();
        }

        outboxCallback(event.getOutboxParent());



    }

    private void outboxCallback(Long id){

        try {
            transactionTemplate.execute(status -> {
                outboxEventRepository.findById(id).ifPresent(outboxEvent -> {
                    outboxEvent.setStatus(OutboxEvent.OutboxEventStatus.PROCESSED);
                });
                return null;
            });
        }
        catch (Exception e){

        }
    }







}
