package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger;


import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/*

триггеры предназначены для организации доставки сигнала до waiting for signal процесса

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












    public void deactivate(){
        active.set(false);
    }


    public void makeFeed(TriggerFeed feed){



        if (!isActive()) return;

        allFeeds.put(feed.getOrigin(), feed);


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

    public ConcurrentHashMap<String, TriggerFeed> getAllFeeds() {
        return allFeeds;
    }

    public void setAllFeeds(ConcurrentHashMap<String, TriggerFeed> allFeeds) {
        this.allFeeds = allFeeds;
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
}
