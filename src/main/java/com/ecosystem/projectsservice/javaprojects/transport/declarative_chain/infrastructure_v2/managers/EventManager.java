package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.events.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


// сервис, отвечающий за регистрацию ивентов в системе, их расшифровку и доставку очередям


@Deprecated
@Service
public class EventManager {





    private Map<String, EventEnvelope> eventRegistry = new HashMap<>();






    // предполагается, что категории внешних ивентов могут быть сконцентрированы в одном месте
    public void registerExternalEvents(List<Class<?
            extends ExternalEvent<? extends ExternalEventContext>>> events){


        for (Class<? extends ExternalEvent<? extends ExternalEventContext>> clazz:events){
            EventQualifier annotation = clazz.getAnnotation(EventQualifier.class);
            if (annotation==null)
                throw new EventManagerException("отсутствует аннотация @EventQualifier");


            EventEnvelope eventEnvelope = new EventEnvelope();

            eventEnvelope.setClazz(clazz);
            eventEnvelope.setEventQualifier(annotation.value());
            eventEnvelope.setEventCategory(EventEnvelope.EventCategory.EXTERNAL);

            eventRegistry.put(annotation.value(), eventEnvelope);




        }

    }

    public void registerInternalEvent(Class<? extends ChainEvent> event){
        EventQualifier annotation = event.getAnnotation(EventQualifier.class);



        if (annotation==null)
            throw new EventManagerException("отсутствует аннотация @EventQualifier");


        EventEnvelope eventEnvelope = new EventEnvelope();

        eventEnvelope.setClazz(event);
        eventEnvelope.setEventQualifier(annotation.value());
        eventEnvelope.setEventCategory(EventEnvelope.EventCategory.INTERNAL);

        eventRegistry.put(annotation.value(), eventEnvelope);




    }








}
