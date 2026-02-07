package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.InternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.triggers.SimpleTriggerData;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/*
задаем стратегию принятия для фазы проверки активности и фазы принятия решения
 */
@Data
@Builder
public class CustomTrigger {

    @Builder.Default
    private AtomicBoolean waiting = new AtomicBoolean(true);

    private UUID correlationId;

    @Builder.Default
    private Instant expirationTime = Instant.now().plusSeconds(1000*60*60);

    // должно быть задано сообщение, которым будет сопровождать триггер ивент
    private String message;

    @Builder.Default
    private ConcurrentHashMap<UUID, TriggerAnswer> answers = new ConcurrentHashMap<>();

    // стратегия при проверке активности - задаем, при каких условиях проверка позволяет сразу закрыть триггер
    // если true - активация outbox с внесением новых данных internal state, если false - переход к фазе ожидания
    private Function<Map<UUID, TriggerAnswer>, Boolean> activityPhaseApprovalStrategy;

    // если true или false - внесение изменений внутри internal data, активация outbox (юзер сам должен пометить, что нужен переход к компенсации)
    private Function<Map<UUID, TriggerAnswer>, Boolean> decisionPhaseApprovalStrategy;

    // триггер может нести, к примеру, набор вариантов на выбор пользователю, или часть контекста
    private SimpleTriggerData triggerExternalData;

    // время ожидания регистраторов активности
    @Builder.Default
    private long activityPhaseWaitingTimeInMs = 500;

    // время ожидания принятия решений
    @Builder.Default
    private long decisionPhaseWaitingTimeInMs = 5000;

    // false означает ожидание
    // true означает запуск процесс или компенсации
    public boolean isWaitingPhaseApproved(){
        if (activityPhaseApprovalStrategy == null){
            throw new IllegalStateException("trigger error -> отсутствует стратегия обработки фазы регистрации активности");
        }

        // defencive copy
        return activityPhaseApprovalStrategy.apply(new HashMap<>(answers));
    }

    public boolean isDecisionPhaseApproved(){
        if (decisionPhaseApprovalStrategy == null){
            throw new IllegalStateException("trigger error -> отсутствует стратегия обработки фазы принятия решения");
        }

        return decisionPhaseApprovalStrategy.apply(new HashMap<>(answers));
    }

    // регистрация по юзеру
    public void registerAnswer(TriggerAnswer answer){
        if (isWaiting()){
            answers.put(answer.getUser(), answer);
        }

    }

    public boolean isWaiting(){
        return waiting.get();
    }







    public void stop(){
        waiting.set(false);
    }

}
