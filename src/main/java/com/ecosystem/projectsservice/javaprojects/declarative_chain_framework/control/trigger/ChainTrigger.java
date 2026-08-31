package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger;


import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/*

триггеры предназначены для организации доставки сигнала до waiting for signal процесса

 */
public abstract class ChainTrigger {



    // если true, триггер готов принимать ответы
    private AtomicBoolean active = new AtomicBoolean(true);

    // когда expiration превышен, триггер получает active = false, и выбрасывается из хранилища
    private Instant expirationTime;

    // к какому процессу относится триггер
    private UUID processId;





    public void stop(){
        active.set(false);
    }






}
