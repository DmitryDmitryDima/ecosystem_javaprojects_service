package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.DeclarativeChainSpringAdapter;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DirectoryRemoveTestChain extends DeclarativeChainSpringAdapter<DirectoryRemoveTestEvent> {

    @Override
    @Async("chainExecutor")
    @EventListener
    public void catchEvent(DirectoryRemoveTestEvent event) {

        System.out.println("directory remove test caught with message "+event.getMessage());



    }

    @Override
    protected void compensationStrategy(DirectoryRemoveTestEvent event) {

    }
}
