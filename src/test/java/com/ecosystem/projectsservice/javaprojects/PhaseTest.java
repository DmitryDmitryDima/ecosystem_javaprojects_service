package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.PhaseStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@SpringBootTest
public class PhaseTest {

    @Test
    public void phase(){


        PhaseStrategy strategy = PhaseStrategy.constructStrategy()
                .addPhase(
                        (answers)->{
                            System.out.println("one");
                            return false;
                        }, 5000
                )

                .addPhase(answers->{
                    System.out.println("two");
                    return false;
                }, 5000)

                .addPhase(answers->{
                    System.out.println("three");
                    return false;
                }, 5000)
                .getStrategy();



        strategy.getActions().forEach(phase -> {
            phase.getAction().apply(null);
        });






        /*
        TestPhaseTrigger trigger = new TestPhaseTrigger();
        PhaseStrategy strategy = new PhaseStrategy();

        strategy.chain.add(new Phase(
                (arg)->{
                    System.out.println("step 1 "+arg);
                    return false;}, 5000));

        strategy.chain.add(new Phase(

                (arg)->{
                    System.out.println("step 2 "+arg);
                    return true;
                    }, 7000));

        strategy.chain.add(new Phase(

                (arg)->{
                    System.out.println("step 3 "+arg);
                    return false;
                }, 7000));


        // на последней фазе мы не учитываем true или false - push выполняется в любом случае, дальнейшие действия
        // происходят на основании
        try (ScheduledExecutorService service = Executors.newScheduledThreadPool(2)){

            List<ScheduledFuture<?>> tasks = new CopyOnWriteArrayList<>();
            long time = 0;
            for (int i = 0; i<strategy.chain.size(); i++){
                Phase phase = strategy.chain.get(i);
                time+=phase.period;
                int stepNum = i;
                tasks.add(service.schedule((

                )->{
                    if (!trigger.active.get()) return;

                    boolean result = phase.phaseCheck.apply("lol "+stepNum);
                    if (result){
                        trigger.active.set(false);
                        // отмена всех действий
                        tasks.forEach(t->{
                            t.cancel(false);
                        });
                        System.out.println("pushing");
                        return;
                    }
                    if (strategy.isLast(stepNum)){
                        trigger.active.set(false);
                        tasks.forEach(t->{
                            t.cancel(false);
                        });
                        System.out.println("pushing at last");
                    }

                }, time, TimeUnit.MILLISECONDS));
            }




        }

         */


    }


    static  class TestPhaseTrigger{
        PhaseStrategyTest strategy;
        private AtomicBoolean active = new AtomicBoolean(true);


    }



    static class PhaseStrategyTest {

        List<Phase> chain = new ArrayList<>();

        boolean isLast(long step){
            return step==chain.size()-1;
        }





    }



    static class Phase {
        long period;
        Function<String, Boolean> phaseCheck;



        Phase(Function<String, Boolean> check, long period){
            this.period = period;
            this.phaseCheck = check;
        }
    }
}
