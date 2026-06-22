package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.event_registry;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.events.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.registry.ChainEventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


// регистрация с помощью аннотации EventQualifier
@Service
public class EventRegistryDefault implements EventRegistry {


    private  final Map<String, Class<?>> storage = new HashMap<>();


    @Override
    public void register(Class<? extends ChainEvent> event) {




        ChainEventQualifier annotation = event
                .getAnnotation(ChainEventQualifier.class);


        if (annotation == null)
            throw new IllegalStateException("Не" +
                    " прописано имя внешнего ивента для цепи." +
                    " Используйте @ChainEventQualifier");

        System.out.println(annotation.value()+" "+event.getName());


        storage.put(annotation.value(), event.getClass());

    }
}
