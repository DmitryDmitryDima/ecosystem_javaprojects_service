package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.test;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.order.Ending;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.order.Opening;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.order.Step;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.DeclarativeChainSpringAdapter;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatarIndex;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatarStatus;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;


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
    protected List<ProcessAvatarIndex> setProcessIndexes(DirectoryAddTestEvent event) {


        ProcessAvatarIndex index = new ProcessAvatarIndex();

        index.setName("projects");

        index.setKey(event.getExternalContext().getProjectId().toString()); // project id in production


        return List.of(index);
    }

    @Override
    protected void compensationStrategy(DirectoryAddTestEvent event) {

        System.out.println(event.getProcessingInfo().getPerformanceStatus());
        System.out.println(event.getProcessingInfo().getDeliveryStatus());

    }


    @Opening(name = "opening", next = "middle")
    public void opening(DirectoryAddTestEvent event){
        System.out.println("hello from opening");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Step(name = "middle", next = "ending")
    public void middle(DirectoryAddTestEvent event){
        System.out.println("hello from middle");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Ending(name = "ending")
    public void ending(DirectoryAddTestEvent event){
        System.out.println("hello from ending");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



}
