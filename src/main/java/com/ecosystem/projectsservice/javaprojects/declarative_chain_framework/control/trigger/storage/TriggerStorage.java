package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.storage;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.ChainTrigger;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.PushStrategy;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.TriggerFeed;

import java.util.UUID;

public interface TriggerStorage {



    void registerTrigger(ChainTrigger trigger);


    void feedTrigger(TriggerFeed feed);


    void pushProcess(UUID processId, PushStrategy strategy);


    void registerPhases(ChainTrigger trigger);


    void removeTrigger(UUID uuid);





    void clear();





}
