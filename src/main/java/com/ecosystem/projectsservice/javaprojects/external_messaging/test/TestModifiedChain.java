package com.ecosystem.projectsservice.javaprojects.external_messaging.test;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.control.ReadLock;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.control.Retry;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.control.WaitingForSignal;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.order.Ending;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.order.Opening;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.order.Step;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.ChainRudder;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.avatar.structure.ProcessAvatar;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.storage.TriggerStorage;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.ChainTrigger;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.PushStrategy;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.TriggerFeed;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.TriggerPhaseStrategy;
import com.ecosystem.projectsservice.javaprojects.external_messaging.message.ExternalMessage;
import com.ecosystem.projectsservice.javaprojects.external_messaging.message.message_category.ProjectEventFromSystemCategory;
import com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains.BroadcastableChain;
import com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains.declarative_messaging.MessageAfter;
import com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains.declarative_messaging.MessageBefore;
import com.ecosystem.projectsservice.javaprojects.external_messaging.types.ExternalMessageType;
import com.ecosystem.projectsservice.javaprojects.external_messaging.types.MessageType;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.PhaseStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Service
@ExternalMessageType(type = MessageType.JAVA_PROJECT_DIRECTORY_MOVE)
public class TestModifiedChain extends BroadcastableChain<TestEvent> {


    @Autowired
    private ObjectMapper mapper;


    @Autowired
    private TriggerStorage triggers;


    private final ExecutorService executor
            = Executors.newVirtualThreadPerTaskExecutor();


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





    }

    @Opening(name = "op", next = "middle")
    @MessageBefore
    public void op(TestEvent event, ChainRudder rudder){


        System.out.println("opening step");

        rudder.setOnStepCrash("end");



        throw new IllegalStateException("error");










    }

    @Step(name = "middle", next = "end")
    @MessageAfter
    @Retry(maxCount = 5)
    //@ReadLock(time = 50)
    public void middle(TestEvent event,
                       ProcessAvatar avatar){


        System.out.println(event.getProcessingInfo().getPerformanceStatus());

        System.out.println("middle step");































    }

    @Ending(name = "end")
    //@WaitingForSignal(time = 30)
    @MessageBefore
    @MessageAfter
    public void end(TestEvent event){


        event.setMessage("message for you");









        System.out.println("ending step");

    }






    @Override
    protected Class<? extends ExternalMessage> messageBind() {

        return ProjectEventFromSystemCategory.class;
    }
}
