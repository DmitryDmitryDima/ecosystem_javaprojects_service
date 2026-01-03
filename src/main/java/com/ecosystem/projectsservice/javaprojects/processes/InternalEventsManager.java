package com.ecosystem.projectsservice.javaprojects.processes;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.CompensationEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.EventName;
import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.BasicQueueEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class InternalEventsManager {




    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ObjectMapper mapper;

    // общий кеш всех внутренних ивентов приложения
    private Map<String, Class<? extends ChainEvent>> chainEventsCache = new HashMap<>();

    // выделяем отдельно имена ивентов, относящихся к событиям для bridge
    private Map<String, Class<? extends ChainEvent>> externalEvents = new HashMap<>();


    public boolean isBridgeEvent(String eventName){
        return externalEvents.containsKey(eventName);
    }


    public void registerBridge(List< Class<? extends ChainEvent>> chainEvents){
        registerEvents(chainEvents, true);


    }

    public void registerEvents(List< Class<? extends ChainEvent>> chainEvents){
        registerEvents(chainEvents, false);
    }



    private void registerEvents(List< Class<? extends ChainEvent>> chainEvents, boolean bridge){




        chainEvents.forEach(eventClass->{
            EventName annotation = eventClass.getAnnotation(EventName.class);
            if (annotation!=null){
                chainEventsCache.put(annotation.value(), eventClass);
                if (bridge){
                    externalEvents.put(annotation.value(), eventClass);
                }

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
            log.info("работа с payload формата "+outboxEvent.getPayload());
            ChainEvent chainEvent = mapper.readValue(outboxEvent.getPayload(), type);

            chainEvent.setOutboxParent(outboxEvent.getId());



            // для удобства обогащаем сущность готовым типом
            if (chainEvent instanceof CompensationEvent compensationEvent){
                compensationEvent.setAfterEventTypeConverted(chainEventsCache.get(compensationEvent.getAfterEventType()));
            }


            log.info("ивент с родителем "+chainEvent.getOutboxParent()+" с типом "+chainEvent.getClass().getName()+" готов к публикации");
            publisher.publishEvent(chainEvent);


        }
        catch (Exception e){
            log.info("ошибка публикации ивента "+e.getMessage());
            throw new IllegalStateException("invalid payload value");
        }

    }



}
