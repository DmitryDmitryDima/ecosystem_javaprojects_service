package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.publisher.ChainPublisher;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.DeclarativeChainSpringAdapter;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorage;
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



    }

    @Override
    protected void compensationStrategy(DirectoryAddTestEvent event) {

    }



}
