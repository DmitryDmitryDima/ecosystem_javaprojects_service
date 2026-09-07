package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModelRepository;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

// todo метод очистки от expired
public class DefaultTriggerStorage implements TriggerStorage {


    private OutboxModelRepository repository;







    private ConcurrentHashMap<UUID, ChainTrigger> storage
            = new ConcurrentHashMap<>();


    public DefaultTriggerStorage(OutboxModelRepository repository){

        this.repository = repository;
    }


    @Override
    public void registerTrigger(ChainTrigger trigger) {





        storage.put(trigger.getProcessId(), trigger);



        // если в триггере есть фазы, то их необходимо зарегистрировать

        registerPhases(trigger);



    }

    @Override
    public void feedTrigger(TriggerFeed feed) {


        ChainTrigger trigger = storage.get(feed.getProcessId());

        if (trigger==null) throw new ReactionException("триггер не найден");


        // phase trigger является наследником reactive trigger
        // его отличие в том, что при положительной реакции следующие фазы столкнутся с тем,
        // что триггер уже был закрыт, и не выполнятся




        boolean needPush = trigger.react(feed);

        if (needPush){
            pushProcess(trigger);
        }


    }


    // по задумке триггер теперь не вносит ничего в payload
    // - следующий шаг сам обращается к результатам

    // push происходит на основе push strategy

    // на этой базе легко сделать персистентность

    @Override
    public void pushProcess(ChainTrigger trigger) {



        if (trigger.getPushStrategy() == PushStrategy.WAITING_FOR_SIGNAL){
            repository.receiveSignalWhileWaitingFor(trigger.getProcessId());

        }

        else if (trigger.getPushStrategy() == PushStrategy.READLOCK){
            repository.receiveSignalWhileLocked(trigger.getProcessId());
        }

    }

    @Override
    public void registerPhases(ChainTrigger trigger) {


        if (trigger.getPhaseStrategy() == null) return;


        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {


            for (var phase:trigger.getPhaseStrategy().getActions()){

                Runnable runnable = ()->{


                    // задержка периода

                    try {
                        Thread.sleep(phase.getMsDelay());
                    } catch (InterruptedException e) {
                        return;
                    }

                    // выполнение действия

                    try {


                        boolean phaseAnswer = false;

                        synchronized (trigger){

                            // ничего не делаем, если триггер был деактивирован
                            if (!trigger.isActive()){
                                return;
                            }

                            phaseAnswer =
                                    phase.getAction().apply(trigger.getAllFeeds());

                            if (phaseAnswer){

                                trigger.deactivate();

                            }
                        }

                        // выносим за synchronized блок

                        if (phaseAnswer){
                            pushProcess(trigger);
                        }







                    }

                    catch (Exception e){
                        e.printStackTrace();
                    }



                };


                executor.submit(runnable);
            }


        }




    }




    @Override
    public void clear() {

        //

    }


}


