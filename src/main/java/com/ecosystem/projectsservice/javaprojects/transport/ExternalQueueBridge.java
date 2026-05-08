package com.ecosystem.projectsservice.javaprojects.transport;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;

import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromSystem;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.UserPersonalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.ChainManager;
import com.ecosystem.projectsservice.javaprojects.repository.OutboxEventRepository;
import com.ecosystem.projectsservice.javaprojects.service.external_values.ExternalValues;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;


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










    @Autowired
    private ExternalValues externalValues;


    // точка регистрации категорий

    @PostConstruct
    public void registerQueueEvents(){

        chainManager.registerExternalEvents(List.of(
                ProjectEventFromUser.class, UserPersonalEvent.class, ProjectEventFromSystem.class
        ));
    }


    @EventListener
    @Async("virtualThreadFactory")
    public void catchProjectSystemEvent(ProjectEventFromSystem projectEventFromSystem){
        try {
            MessagePostProcessor postProcessor = (message )->{
                message.getMessageProperties().setHeader("event_type", projectEventFromSystem.getType());
                return message;
            };

            String payload = mapper.writeValueAsString(projectEventFromSystem);

            rabbitTemplate.convertAndSend(externalValues.getSystemProjectsEventsExchangeName(), "", payload, postProcessor);


        }
        catch (Exception e){
            e.printStackTrace();
        }

        if (projectEventFromSystem.getOutboxParent()!=null){
            outboxCallback(projectEventFromSystem.getOutboxParent());
        }
    }

    @EventListener
    @Async("virtualThreadFactory")
    public void catchUserActivityEvent(UserPersonalEvent event){
        System.out.println("user event ");
        try {
            MessagePostProcessor postProcessor = (message )->{
                message.getMessageProperties().setHeader("event_type", event.getType());
                return message;
            };

            String payload = mapper.writeValueAsString(event);

            rabbitTemplate.convertAndSend(externalValues.getUsersActivityExchangeName(), "", payload, postProcessor);


        }
        catch (Exception e){
            e.printStackTrace();
        }

        if (event.getOutboxParent()!=null){
            outboxCallback(event.getOutboxParent());
        }
    }



    @EventListener
    @Async("virtualThreadFactory")
    public void catchProjectUserEvent(ProjectEventFromUser event){
        System.out.println("project event "+event);
        try {
            MessagePostProcessor postProcessor = (message )->{
                message.getMessageProperties().setHeader("event_type", event.getType());
                return message;
            };

            String payload = mapper.writeValueAsString(event);

            rabbitTemplate.convertAndSend(externalValues.getUsersProjectsEventsExchangeName(), "", payload, postProcessor);


        }
        catch (Exception e){
            e.printStackTrace();
        }

        outboxCallback(event.getOutboxParent());



    }

    private void outboxCallback(UUID id){

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
