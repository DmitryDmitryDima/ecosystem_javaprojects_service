package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.InternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.triggers.TriggerDataEnvelope;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public abstract class Trigger  {


    private UUID correlationId;
    public Trigger(UUID correlationId, String message){

        this.triggerMessage = message;
        this.correlationId = correlationId;
    }


    private String triggerMessage;



    // триггер удаляется автоматически при просрочке
    private Instant expiredAt = Instant.now().plusSeconds(60*60); // дефолт - час после expire












    private AtomicBoolean active = new AtomicBoolean(true);


    public void stop(){
        active.set(false);
    }

    public boolean isActive(){
        return active.get();

    }



    public abstract void consumeAnswer(UserTriggerAnswer answer);


    // анализирует - достигнут ли консенсус. Метод вызывается два раза - спустя одну секунду и спустя 3-4 секунды
    // консенсус - интерпретация наличия пустого ответа вначале и полного ответа в конце
    // для некоторых ситуаций, к примеру если опрос проводит система. отсутствие даже пустого ответа будет означать откат
    public abstract boolean hasApproval();


    public abstract TriggerDataEnvelope getTriggerEnvelope();

    // внесение дополнительных данных во внутренние данные, если это необходимо
    // для триггеров. работающих с каким либо вариантом, можно ориентироваться на наличие ответа в принципе
    public abstract void onApprove(InternalEventData internalEventData);










}
