package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.control.Retry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.control.TimeLimit;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.order.Ending;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.order.Opening;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.DeclarativeChainSpringAdapter;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatarIndex;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TestChain extends DeclarativeChainSpringAdapter<TestChainEvent> {


    @Override
    protected List<ProcessAvatarIndex> setProcessIndexes(TestChainEvent event) {
        ProcessAvatarIndex index = new ProcessAvatarIndex();

        index.setName("randoms");

        index.setKey(UUID.randomUUID().toString()); // project id in production


        return List.of(index);
    }

    @Override
    @Async("chainExecutor")
    @EventListener
    public void catchEvent(TestChainEvent event) {
        super.processEvent(event);
    }


    @Opening(name = "opening", next = "ending")
    @TimeLimit(time = 2)
    public void opening(TestChainEvent event){


        System.out.println("hello from opening - test chain");
        while (!Thread.currentThread().isInterrupted()){

            System.out.println("opening");
        }



    }


    @Ending(name = "ending")
    public void ending(TestChainEvent event){

        System.out.println("hello from ending - test chain");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    protected void compensationStrategy(TestChainEvent event) {


        System.out.println("ВХОД В КОМПЕНСАЦИОННЫЙ СЦЕНАРИЙ ДЛЯ ШАГА "
                +event.getProcessingInfo().getCurrentStep());

        System.out.println(event.getProcessingInfo().getPerformanceStatus());
        System.out.println(event.getProcessingInfo().getDeliveryStatus());

        System.out.println(event.getProcessingInfo().getCurrentStep()+" шаг в компенсации");


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }



    }
}
