package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.event_registry;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.registry.ChainEventQualifier;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events.ChainEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


// регистрация с помощью аннотации EventQualifier

public class EventRegistryDefault implements EventRegistry {


    private final Map<String, Class<? extends ChainEvent>> storage = new HashMap<>();


    @Override
    public void register(Class<? extends ChainEvent> event) {




        ChainEventQualifier annotation = event
                .getAnnotation(ChainEventQualifier.class);


        if (annotation == null)
            throw new IllegalStateException("Не" +
                    " прописано имя внешнего ивента для цепи." +
                    " Используйте @ChainEventQualifier");

        System.out.println(annotation.value()+" "+event.getName());


        storage.put(annotation.value(), event);

    }

    @Override
    public Optional<Class<? extends ChainEvent>> getRegisteredClass(String type) {


        Class<? extends ChainEvent> eventClass = storage.get(type);

        if (eventClass == null) return Optional.empty();

        else return Optional.of(eventClass);
    }


}
