package com.ecosystem.projectsservice.javaprojects.external_messaging.test;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.control.ReadExpiration;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.control.ReadLock;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.control.Retry;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.control.WaitingForSignal;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.order.Ending;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.order.Opening;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.order.Step;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.ProcessAvatar;
import com.ecosystem.projectsservice.javaprojects.external_messaging.context.ExternalContext;
import com.ecosystem.projectsservice.javaprojects.external_messaging.data.ExternalData;
import com.ecosystem.projectsservice.javaprojects.external_messaging.message.ExternalMessage;
import com.ecosystem.projectsservice.javaprojects.external_messaging.message.message_category.ProjectEventFromSystemCategory;
import com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains.BroadcastableChain;
import com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains.declarative_messaging.MessageAfter;
import com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains.declarative_messaging.MessageBefore;
import com.ecosystem.projectsservice.javaprojects.external_messaging.types.ExternalMessageType;
import com.ecosystem.projectsservice.javaprojects.external_messaging.types.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;



@Service
@ExternalMessageType(type = MessageType.JAVA_PROJECT_DIRECTORY_MOVE)
public class TestModifiedChain extends BroadcastableChain<TestEvent> {


    @Autowired
    private ObjectMapper mapper;


    @Override
    @Async("chainExecutor")
    @EventListener
    public void catchEvent(TestEvent event) {
        super.processEvent(event);
    }

    @Override
    protected void compensationStrategy(TestEvent event) {


        System.out.println(event.getProcessingInfo().getDeliveryStatus());

        System.out.println(event.getProcessingInfo().getPerformanceStatus());

        throw new IllegalStateException("sudden crash inside compensation");



    }

    @Opening(name = "op", next = "middle")
    @MessageBefore
    @MessageAfter
    public void op(TestEvent event){

        System.out.println("opening mod");
        System.out.println(event.getExternalContext().getCorrelationId());

        event.setMessage("message from op");
    }

    @Step(name = "middle", next = "end")
    @MessageAfter
    public void middle(TestEvent event,
                       ProcessAvatar avatar){

        System.out.println("middle mod");













    }

    @Ending(name = "end")
    @MessageBefore
    @MessageAfter
    public void end(TestEvent event){






        System.out.println("ending mod");

    }






    @Override
    protected Class<? extends ExternalMessage> messageBind() {

        return ProjectEventFromSystemCategory.class;
    }
}
