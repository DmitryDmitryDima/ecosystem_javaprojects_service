package com.ecosystem.projectsservice.javaprojects.processes;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.CompensationEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.EventName;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InternalEventsManager {




    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ObjectMapper mapper;

    private Map<String, Class<? extends ChainEvent>> chainEventsCache = new HashMap<>();




    public void registerEvents(List< Class<? extends ChainEvent>> chainEvents){




        chainEvents.forEach(eventClass->{
            EventName annotation = eventClass.getAnnotation(EventName.class);
            if (annotation!=null){
                chainEventsCache.put(annotation.value(), eventClass);

            }
            else {
                throw new IllegalStateException("missing event name");
            }
        });




    }




    // конвертируем outbox запись во внутреннее событие какой-либо из цепочек (каждая цепочка начинает то или иное событие асинхронно)
    public void serializeAndPublish(OutboxEvent outboxEvent){
        Class<? extends ChainEvent> type = chainEventsCache.get(outboxEvent.getType());
        if (type==null){
            throw new IllegalStateException("missing chain event. Check name consistency");
        }

        try {

            ChainEvent chainEvent = mapper.readValue(outboxEvent.getPayload(), type);

            chainEvent.setOutboxParent(outboxEvent.getId());

            // для удобства обогащаем сущность готовым типом
            if (chainEvent instanceof CompensationEvent compensationEvent){
                compensationEvent.setAfterEventTypeConverted(chainEventsCache.get(compensationEvent.getAfterEventType()));
            }

            publisher.publishEvent(chainEvent);

        }
        catch (Exception e){
            System.out.println(e.getMessage());
            throw new IllegalStateException("invalid payload value");
        }

    }



}
