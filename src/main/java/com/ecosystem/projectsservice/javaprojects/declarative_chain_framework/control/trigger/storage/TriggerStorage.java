package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.storage;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.ChainTrigger;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.TriggerFeed;

public interface TriggerStorage {



    void registerTrigger(ChainTrigger trigger);


    void feedTrigger(TriggerFeed feed);


    void pushProcess(ChainTrigger trigger);


    void registerPhases(ChainTrigger trigger);





    void clear();





}
