package com.ecosystem.projectsservice.javaprojects.external_messaging.bridge;


import com.ecosystem.projectsservice.javaprojects.external_messaging.context.context_category.ProjectEventFromSystemContextCategory;
import com.ecosystem.projectsservice.javaprojects.external_messaging.message.message_category.ProjectEventFromSystemCategory;
import com.ecosystem.projectsservice.javaprojects.service.external_values.MessageQueueExternals;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class ExternalQueueBroadcastBridge {



    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;



    @Autowired
    private MessageQueueExternals externalValues;




    @EventListener
    @Async("virtualThreadFactory")
    public void sendProjectEventFromSystem(ProjectEventFromSystemCategory event){




        try {
            MessagePostProcessor postProcessor = (message )->{
                message
                        .getMessageProperties().setHeader("event_type",
                                event.getType());
                return message;
            };

            String payload = mapper.writeValueAsString(event);

            System.out.println(payload+" is ready for external chain");

            /*

            rabbitTemplate.convertAndSend(externalValues
                    .getSystemProjectsEventsExchangeName(),
                    "", payload, postProcessor);

             */


        }
        catch (Exception e){
            e.printStackTrace();
        }


    }








}
