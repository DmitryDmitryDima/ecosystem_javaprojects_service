package com.ecosystem.projectsservice.javaprojects.external_messaging.test;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.control.ReadLock;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.control.Retry;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.control.WaitingForSignal;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.order.Ending;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.order.Opening;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.order.Step;
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
    public void op(TestEvent event){


        System.out.println("opening step");



        /*





        TriggerPhaseStrategy strategy = TriggerPhaseStrategy.constructStrategy()

                .addPhase((answers-> {
                    System.out.println("Тестовая фаза 1 ");

                    boolean found = false;

                    for (var feed:answers.values()){

                        if (feed.getFirst().getData().equals("hello")){
                            found = true;

                            System.out.println("условие пуша выполнено - пушим");
                        }

                    }

                    if (!found){
                        System.out.println("условие для пуша не выполнено");
                    }

                    return found;

                }), 5_000)





                .addPhase((answers)->{

                    System.out.println("Тестовая фаза 2 ");

                    boolean found = false;

                    for (var feed:answers.values()){

                        if (feed.getFirst().getData().equals("hello")){
                            found = true;

                            System.out.println("условие пуша выполнено - пушим");
                        }

                    }

                    if (!found){
                        System.out.println("условие для пуша не выполнено");
                    }

                    return found;
                }, 10_000)



                .getStrategy();


        ChainTrigger trigger = ChainTrigger
                .builder()

                .pushStrategy(PushStrategy.READLOCK)
                .processId(event.getProcessId())
                .expiration(Instant.now().plusSeconds(50))
                .phaseStrategy(strategy)
                .reaction(answers->{
                    System.out.println("reaction received");

                    return false;
                })

                .construct();


        triggers.registerTrigger(trigger);









        event.setMessage("message from op");


        CompletableFuture.delayedExecutor(3000, TimeUnit.MILLISECONDS, executor).execute(

                ()->{

                    triggers.feedTrigger(new TriggerFeed(event.getProcessId(),
                            "hello1", "dima"));
                }
        );

        CompletableFuture.delayedExecutor(4000,
                TimeUnit.MILLISECONDS, executor).execute(

                ()->{

                    triggers.feedTrigger(new TriggerFeed(event.getProcessId(),
                            "hello2", "dima1"));


                }
        );

        CompletableFuture.delayedExecutor(10000,
                TimeUnit.MILLISECONDS, executor).execute(

                ()->{

                    triggers.feedTrigger(new TriggerFeed(event.getProcessId(),
                            "hello", "dima"));


                }
        );

         */








    }

    @Step(name = "middle", next = "end")
    @MessageAfter
    @Retry(maxCount = 5)
    //@ReadLock(time = 50)
    public void middle(TestEvent event,
                       ProcessAvatar avatar){


        System.out.println(event.getProcessingInfo().getPerformanceStatus());

        throw new IllegalStateException("fuck!");



        /*


        System.out.println("middle step");


        triggers.removeTrigger(event.getProcessId());



        TriggerPhaseStrategy strategy = TriggerPhaseStrategy.constructStrategy()

                .addPhase((answers-> {
                    System.out.println("Тестовая фаза 1");

                    return false;

                }), 2_000)

                .addPhase((answers-> {
                    System.out.println("Тестовая фаза 2 ");

                    return false;

                }), 3_000)



                .getStrategy();


        ChainTrigger trigger = ChainTrigger
                .builder()

                .pushStrategy(PushStrategy.WAITING_FOR_SIGNAL)
                .processId(event.getProcessId())
                .expiration(Instant.now().plusSeconds(50))
                .phaseStrategy(strategy)

                .reaction((answers)->{

                    System.out.println("instant reaction");

                    boolean found = false;

                    for (var feed:answers.values()){

                        if (feed.getFirst().getData().equals("hello")){

                            System.out.println("положительная реакция");

                            found = true;


                        }
                    }

                    if (!found){
                        System.out.println("отрицательная реакция");
                    }

                    return found;

                })

                .construct();


        triggers.registerTrigger(trigger);


        CompletableFuture.delayedExecutor(10000,
                TimeUnit.MILLISECONDS, executor).execute(

                ()->{

                    triggers.feedTrigger(new TriggerFeed(event.getProcessId(),
                            "hello1", "dima"));


                }
        );

        CompletableFuture.delayedExecutor(12000,
                TimeUnit.MILLISECONDS, executor).execute(

                ()->{

                    triggers.feedTrigger(new TriggerFeed(event.getProcessId(),
                            "hello", "dima"));


                }
        );

         */



























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
