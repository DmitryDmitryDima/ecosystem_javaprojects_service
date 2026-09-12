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

    private final ExecutorService threadManager
            = Executors.newVirtualThreadPerTaskExecutor();


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


        if (storage.containsKey(trigger.getProcessId())){
            throw new TriggerStorageException("триггер для заданного id уже существует");
        }





        storage.put(trigger.getProcessId(), trigger);



        // если в триггере есть фазы, то их необходимо зарегистрировать

        registerPhases(trigger);



    }

    @Override
    public void feedTrigger(TriggerFeed feed) {


        ChainTrigger trigger = storage.get(feed.getProcessId());

        if (trigger==null) throw new ReactionException("триггер не найден");







        boolean needPush = trigger.react(feed);

        // на данном этапе триггер гарантированно деактивирован реакцией
        if (needPush){
            pushProcess(trigger.getProcessId(), trigger.getPushStrategy());
        }


    }


    // по задумке триггер теперь не вносит ничего в payload
    // - следующий шаг сам обращается к результатам

    // push происходит на основе push strategy

    // на этой базе легко сделать персистентность

    @Override
    public void pushProcess(UUID processId, PushStrategy strategy) {


        try {
            if (strategy == PushStrategy.WAITING_FOR_SIGNAL) {


                repository.receiveSignalWhileWaitingFor(processId);

            } else if (strategy == PushStrategy.READLOCK) {
                repository.receiveSignalWhileLocked(processId);
            }
        }

        catch (Exception e){
            throw new TriggerStorageException("Ошибка push транзакции: "+e.getMessage());
        }

    }

    @Override
    public void registerPhases(ChainTrigger trigger) {


        if (trigger.getPhaseStrategy() == null) return;





        for (var phase:trigger.getPhaseStrategy().getActions()){



            Executor delayedExecutor
                    = CompletableFuture
                    .delayedExecutor(phase.getMsDelay(), TimeUnit.MILLISECONDS, threadManager);



            Runnable runnable = ()->{


                // задержка периода

                try {


                    if (!trigger.isActive()) return;


                    // выполняем фазу, используя snapshot
                    boolean phaseAnswer = phase.getAction().apply(trigger.getAllFeeds());

                    if (phaseAnswer){
                        boolean deactivationResult = trigger.deactivate();

                        if (deactivationResult){
                            pushProcess(trigger.getProcessId(), trigger.getPushStrategy());
                        }
                    }








                }

                catch (Exception e) {

                    String log = "Ошибка фазы: "+e.getMessage();

                    phase.setPhaseLog(log);
                    phase.setResultingException(e);


                }





            };

            Future<?> future = CompletableFuture.runAsync(runnable, delayedExecutor);




            trigger.submitPhase(future);




        }









    }


    // удаляем триггер из хранилища
    @Override
    public void removeTrigger(UUID uuid) {
        ChainTrigger trigger = storage.remove(uuid);

        if (trigger!=null){

            trigger.deactivate();

        }
    }


    @Override
    public void clear() {

        //


        storage.entrySet().removeIf(entry -> {

            if (entry.getValue().isExpired()){

                entry.getValue().deactivate();

                return true;


            }


            return false;
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


