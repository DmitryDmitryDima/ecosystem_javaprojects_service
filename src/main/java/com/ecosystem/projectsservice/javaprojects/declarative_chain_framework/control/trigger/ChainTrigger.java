package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger;


import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.PhaseStrategy;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
    private AtomicBoolean active = new AtomicBoolean(true);

    // когда expiration превышен, триггер получает active = false, и выбрасывается из хранилища
    private Instant expirationTime;

    // к какому процессу относится триггер
    private UUID processId;


    private ConcurrentHashMap<String, TriggerFeed> allFeeds
            = new ConcurrentHashMap<>();



    private Function<Map<String, TriggerFeed>, Boolean> onFeedReaction;

    private TriggerPhaseStrategy phaseStrategy;












    public void deactivate(){
        active.set(false);
    }


    public synchronized boolean react(TriggerFeed feed){



        if (!isActive())
            throw new ReactionException("триггер был остановлен и больше не принимает ответов");

        allFeeds.put(feed.getOrigin(), feed);

        if (onFeedReaction == null) return false; // стратегии может не быть,
        // например в классе наследнике - фазовом триггере

        boolean reaction = onFeedReaction.apply(getAllFeeds());

        if (reaction){
            deactivate();
        }

        return reaction;




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
    public Map<String, TriggerFeed> getAllFeeds() {
        return Map.copyOf(allFeeds);
    }



    public Function<Map<String, TriggerFeed>, Boolean> getOnFeedReaction() {
        return onFeedReaction;
    }

    public void setOnFeedReaction(Function<Map<String, TriggerFeed>, Boolean> onFeedReaction) {
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
}
