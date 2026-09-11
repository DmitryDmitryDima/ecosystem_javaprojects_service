package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/*

триггеры предназначены для организации доставки сигнала до waiting for signal или readlock процесса

 */
public class ChainTrigger {


    /*
     стратегия активации следующего шага:

     - waiting for signal - строгий сценарий - ищем waiting for ивент, меняем на waiting
     - readlock - нестрогий сценарий,
      где push означает лишь смену lock_until на now, досрочно активируя ожидающий шаг
     */

    private PushStrategy pushStrategy = PushStrategy.WAITING_FOR_SIGNAL;



    // если true, триггер готов принимать ответы
    private final AtomicBoolean active = new AtomicBoolean(true);

    // когда expiration превышен, триггер получает active = false, и выбрасывается из хранилища
    private Instant expirationTime = Instant.now().plusSeconds(60);

    // к какому процессу относится триггер
    private UUID processId;


    private ConcurrentHashMap<String, List<TriggerFeed>> allFeeds
            = new ConcurrentHashMap<>();



    private Function<Map<String, List<TriggerFeed>>, Boolean> onFeedReaction;

    private TriggerPhaseStrategy phaseStrategy;


    public ChainTrigger(UUID processId,
                        Instant expirationTime,
                        PushStrategy pushStrategy,
                        Function<Map<String, List<TriggerFeed>>, Boolean> onFeedReaction,
                        TriggerPhaseStrategy phaseStrategy) {
        this.processId = processId;
        this.expirationTime = expirationTime;
        this.pushStrategy = pushStrategy;
        this.onFeedReaction = onFeedReaction;
        this.phaseStrategy = phaseStrategy;
    }

    public void deactivate(){
        active.set(false);

        if (phaseStrategy!=null){
            phaseStrategy.cancelPhases();
        }
    }


    // сохраняем коллбэк активной фазы для создания возможности ее досрочной остановки
    public void submitPhase(Future<?> phase){

        if (phaseStrategy!=null){
            phaseStrategy.addActivePhase(phase);
        }
    }


    public synchronized boolean react(TriggerFeed feed){


        synchronized (this){

            if (!isActive())
                throw new ReactionException("триггер был остановлен и больше не принимает ответов");




            allFeeds.compute(feed.getOrigin(), (origin, list)->{

                if (list == null){
                    List<TriggerFeed> newList = new ArrayList<>();

                    newList.add(feed);

                    return newList;
                }

                else {
                    list.add(feed);
                    return list;
                }



            });

            if (onFeedReaction == null) return false; // стратегии может не быть,
        }

        boolean reaction;

        try {
            reaction = onFeedReaction.apply(getAllFeeds()); // работаем с копией
        }
        catch (Exception e){

            throw new ReactionException("ошибка в реакции: "+e.getMessage());

        }

        synchronized (this){

            if (!isActive())
                throw new ReactionException("триггер был остановлен во время обработки реакции");


            if (reaction){
                deactivate();
            }

            return reaction;
        }


    }


    public boolean isActive(){
        return active.get();
    }





    public Instant getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Instant expirationTime) {
        this.expirationTime = expirationTime;
    }


    // snapshot
    public Map<String, List<TriggerFeed>> getAllFeeds() {
        return Map.copyOf(allFeeds);
    }


    public boolean isExpired(){

        return Instant.now().isAfter(expirationTime);
    }



    public Function<Map<String, List<TriggerFeed>>, Boolean> getOnFeedReaction() {
        return onFeedReaction;
    }

    public void setOnFeedReaction(Function<Map<String, List<TriggerFeed>>, Boolean> onFeedReaction) {
        this.onFeedReaction = onFeedReaction;
    }




    public PushStrategy getPushStrategy() {
        return pushStrategy;
    }

    public void setPushStrategy(PushStrategy pushStrategy) {
        this.pushStrategy = pushStrategy;
    }


    public UUID getProcessId() {
        return processId;
    }

    public void setProcessId(UUID processId) {
        this.processId = processId;
    }


    public TriggerPhaseStrategy getPhaseStrategy() {
        return phaseStrategy;
    }

    public void setPhaseStrategy(TriggerPhaseStrategy phaseStrategy) {
        this.phaseStrategy = phaseStrategy;
    }


    public static ChainTriggerBuilder builder(){
        return new ChainTriggerBuilder();
    }





    public static class ChainTriggerBuilder{


        private PushStrategy pushStrategy = PushStrategy.WAITING_FOR_SIGNAL;

        private Instant expirationTime = Instant.now().plusSeconds(60);

        private UUID processId;

        private Function<Map<String, List<TriggerFeed>>, Boolean> onFeedReaction;

        private TriggerPhaseStrategy phaseStrategy;



        public ChainTriggerBuilder reaction(Function<Map<String, List<TriggerFeed>>, Boolean> reaction){

            this.onFeedReaction = reaction;
            return this;
        }

        public ChainTriggerBuilder phaseStrategy(TriggerPhaseStrategy strategy){
            this.phaseStrategy = strategy;
            return this;
        }

        public ChainTriggerBuilder expiration(Instant time){
            this.expirationTime = time;

            return this;
        }


        public ChainTriggerBuilder processId(UUID id){

            this.processId = id;


            return this;
        }

        public ChainTriggerBuilder pushStrategy(PushStrategy strategy){

            this.pushStrategy = strategy;

            return this;
        }


        public ChainTrigger construct(){

            return new ChainTrigger(processId,
                    expirationTime,
                    pushStrategy,
                    onFeedReaction,
                    phaseStrategy);

        }









    }




}
