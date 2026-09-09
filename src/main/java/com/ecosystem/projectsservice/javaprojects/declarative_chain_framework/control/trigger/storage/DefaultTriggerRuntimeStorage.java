package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.storage;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.ChainTrigger;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.PushStrategy;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.ReactionException;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure.TriggerFeed;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModelRepository;

import java.util.UUID;
import java.util.concurrent.*;

// todo метод очистки от expired
public class DefaultTriggerRuntimeStorage implements TriggerStorage {


    private OutboxModelRepository repository;

    private final ExecutorService threadManager = Executors.newVirtualThreadPerTaskExecutor();


    private final ConcurrentHashMap<UUID, ChainTrigger> storage
            = new ConcurrentHashMap<>();


    public DefaultTriggerRuntimeStorage(OutboxModelRepository repository){

        this.repository = repository;
    }




    @Override
    public void registerTrigger(ChainTrigger trigger) {


        if (trigger.getProcessId()==null){
            throw new TriggerStorageException("не указан uuid процесса");
        }





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


        for (var phase:trigger.getPhaseStrategy().getActions()){

            Runnable runnable = ()->{


                // задержка периода

                try {
                    Thread.sleep(phase.getMsDelay());

                    if (!trigger.isActive()){
                        return;
                    }




                    boolean phaseAnswer = phase.getAction().apply(trigger.getAllFeeds());

                    synchronized (trigger){
                        if (!trigger.isActive()){
                            return;
                        }

                        if (phaseAnswer){
                            trigger.deactivate();


                        }
                    }

                    // вызов вне лока
                    pushProcess(trigger);
















                }
                catch (InterruptedException e){
                    String log = "фаза прервана";

                    phase.setPhaseLog(log);

                    Thread.currentThread().interrupt();




                }
                catch (Exception e) {

                    String log = "Ошибка фазы: "+e.getMessage();

                    phase.setPhaseLog(log);
                    phase.setResultingException(e);


                }





            };


            Future<?> future = threadManager.submit(runnable);

            trigger.submitPhase(future);




        }









    }




    @Override
    public void clear() {

        //


        storage.entrySet().removeIf(entry -> {

            if (entry.getValue().isExpired()){

                entry.getValue().deactivate();


            }


            return false;
        });




        storage.forEach((uuid, trigger) -> {

            // если триггер просрочен, он уходит из хранилища
            if (trigger.isExpired()){

                trigger.deactivate();

                storage.remove(uuid);
            }

        });

    }


    // runtime хранилище должно быть уничтожено, так как задействуется executor сервис для фаз
    public void destroy(){

        threadManager.shutdown();

        try {
            if (!threadManager.awaitTermination(10, TimeUnit.SECONDS)) {
                threadManager.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadManager.shutdownNow();
            Thread.currentThread().interrupt();
        }

    }


    public OutboxModelRepository getRepository() {
        return repository;
    }

    public void setRepository(OutboxModelRepository repository) {
        this.repository = repository;
    }
}


