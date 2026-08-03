package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.order.Ending;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.order.Opening;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.DeclarativeChainSpringAdapter;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class DirectoryAddTestChain
        extends DeclarativeChainSpringAdapter<DirectoryAddTestEvent> {




    @Override
    @Async("chainExecutor")
    @EventListener
    public void catchEvent(DirectoryAddTestEvent event) {


        System.out.println("directory add test caught with message "+event.getMessage());

        super.processEvent(event);



    }

    @Override
    protected void compensationStrategy(DirectoryAddTestEvent event) {

        System.out.println(event.getProcessingInfo().getPerformanceStatus());
        System.out.println(event.getProcessingInfo().getDeliveryStatus());

    }


    @Opening(name = "opening", next = "ending")
    public void opening(DirectoryAddTestEvent event){
        System.out.println("hello from opening");
    }


    @Ending(name = "ending")
    public void ending(DirectoryAddTestEvent event){
        System.out.println("hello from ending");
    }



}
