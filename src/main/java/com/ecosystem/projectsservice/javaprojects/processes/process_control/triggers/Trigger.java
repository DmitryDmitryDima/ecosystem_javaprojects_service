package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.triggers.SimpleTriggerData;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class Trigger {

    // если true - триггер готов принимать ответы
    @Builder.Default
    private AtomicBoolean waiting = new AtomicBoolean(true);

    // uuid родительского процесса
    private UUID correlationId;

    // нужен ли polling ивент в процессе? (если да - это, как правило, означает, что ожидается реакция от ui, хотя в теории это может быть и система)
    private boolean needPollingMessage = true;


    @Builder.Default
    private Instant expirationTime = Instant.now().plusSeconds(1000*60*60);

    // должно быть задано сообщение, которым будет сопровождаться polling ивент
    private String message;

    @Builder.Default
    private ConcurrentHashMap<String, TriggerAnswer> answers = new ConcurrentHashMap<>();


    public Map<String, TriggerAnswer> getAnswers() {
        // defensive copy
        return new HashMap<>(answers);
    }

    // data для polling ивента
    // триггер может нести, к примеру, набор вариантов на выбор пользователю, или часть контекста
    private SimpleTriggerData triggerExternalData;


    // регистрация ответа по юзеру или по псевдониму системы
    public void registerAnswer(TriggerAnswer answer){
        if (isWaiting()){
            if (answer.getUser()!=null){

                answers.put(answer.getUser().toString(), answer);
            }
            else {
                answers.put(answer.getPseudonym(), answer);
            }

        }

    }

    public boolean isWaiting(){
        return waiting.get();
    }

    public void stop(){
        waiting.set(false);
    }





}
