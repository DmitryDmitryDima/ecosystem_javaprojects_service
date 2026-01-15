package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ChainManager {


    private final Map<String, Class<? extends DeclarativeChainEvent<? extends ExternalEventContext,
                ? extends ExternalEventData, ? extends InternalEventData>>> allInternalEvents = new HashMap<>();

    private final Map<String, Class<? extends ExternalEvent<? extends ExternalEventContext>>> allExternalEvents = new HashMap<>();

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ApplicationEventPublisher publisher;

    public void registerInternalEvent(String name, Class<? extends DeclarativeChainEvent<? extends ExternalEventContext,
            ? extends ExternalEventData, ? extends InternalEventData>> clazz){
        System.out.println(name+" registered");
        allInternalEvents.put(name, clazz);
    }

    public void registerExternalEvents(List<Class<? extends ExternalEvent<? extends ExternalEventContext>>> classes){
        for (Class<? extends ExternalEvent<? extends ExternalEventContext>> clazz:classes){
            EventQualifier annotation = clazz.getAnnotation(EventQualifier.class);
            if (annotation==null) throw new IllegalStateException("отсутствует аннотация @EventQualifier");
            allExternalEvents.put(annotation.value(), clazz);

        }
    }

    public void deserializeAndPublish(OutboxEvent outboxEvent){

        try {
            if (allInternalEvents.containsKey(outboxEvent.getType())){
                Class<? extends DeclarativeChainEvent<? extends ExternalEventContext,
                        ? extends ExternalEventData, ? extends InternalEventData>> clazz = allInternalEvents.get(outboxEvent.getType());
                DeclarativeChainEvent<? extends ExternalEventContext,
                        ? extends ExternalEventData, ? extends InternalEventData> deserializedEvent = mapper.readValue(outboxEvent.getPayload(), clazz);

                deserializedEvent.getInternalData().setOutboxParent(outboxEvent.getId()); // для callback
                publisher.publishEvent(deserializedEvent);

            }
            if (allExternalEvents.containsKey(outboxEvent.getType())){
                Class<? extends ExternalEvent<? extends ExternalEventContext>> clazz = allExternalEvents.get(outboxEvent.getType());


                ExternalEvent<? extends ExternalEventContext> deserializedEvent = mapper.readValue(outboxEvent.getPayload(), clazz);
                deserializedEvent.setOutboxParent(outboxEvent.getId());
                publisher.publishEvent(deserializedEvent);


            }
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("payload error "+e.getMessage());
        }


    }
}
