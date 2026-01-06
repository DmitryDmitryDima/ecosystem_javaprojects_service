package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ChainManager {


    private Map<String, Class<? extends DeclarativeChainEvent<? extends ExternalEventContext>>> allInternalEvents = new HashMap<>();

    public void registerEvent(String name, Class<? extends DeclarativeChainEvent<? extends ExternalEventContext>> clazz){
        System.out.println(name+" registered");
        allInternalEvents.put(name, clazz);
    }
}
