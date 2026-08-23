package com.ecosystem.projectsservice.javaprojects.external_messaging.test;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.order.Ending;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.order.Opening;
import com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains.BroadcastableChain;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TestModifiedChain extends BroadcastableChain<TestEvent> {


    @Override
    @Async("chainExecutor")
    @EventListener
    public void catchEvent(TestEvent event) {
        super.processEvent(event);
    }

    @Override
    protected void compensationStrategy(TestEvent event) {



    }

    @Opening(name = "op", next = "end")
    public void op(TestEvent event){

        System.out.println("opening mod");
        System.out.println(event.getExternalContext().getCorrelationId());
    }

    @Ending(name = "end")
    public void end(TestEvent event){
        System.out.println("ending mod");

    }
}
