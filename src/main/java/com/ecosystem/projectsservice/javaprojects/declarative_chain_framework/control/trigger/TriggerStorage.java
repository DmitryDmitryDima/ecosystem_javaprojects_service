package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger;


import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.Trigger;

public interface TriggerStorage {



    void registerTrigger(ChainTrigger trigger);


    void feedTrigger(TriggerFeed feed);


    void pushProcess(ChainTrigger trigger);


}
