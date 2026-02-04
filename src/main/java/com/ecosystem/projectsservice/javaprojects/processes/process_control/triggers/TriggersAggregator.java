package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.InternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.EventStatus;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.triggers.TriggerDataEnvelope;
import com.ecosystem.projectsservice.javaprojects.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


// данная система работает в совокупности с механизмом waiting for
// юзер, проектируя цепочку, должен создать триггер, который будет активировать какими-либо действиями из ui.
// Активация триггера позволяет перевести ожидающий Outbox event из состояния waiting_for_external в waiting
// при устаревании outbox автоматически запускается компенсационное событие, сигнализирующее, что условие для запуска следующего шага не выполнено
@Service
public class TriggersAggregator {


    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private OutboxEventRepository outboxEventRepository;







    private ConcurrentHashMap<UUID, Trigger> triggers = new ConcurrentHashMap<>();


    public void createTrigger(Trigger trigger){
        triggers.put(trigger.getCorrelationId(), trigger);
    }

    public void activateTrigger(DeclarativeChainEvent<?,?,?> chainEvent,
                                ExternalEvent externalEvent,
                                ExternalEventType externalType) throws Exception{


        System.out.println("attempt to activate trigger");

        Trigger trigger = getTrigger(chainEvent.getContext().getCorrelationId());

        if (trigger==null) return;


        externalEvent.setStatus(EventStatus.POLLING);
        externalEvent.setType(externalType.getName());



        TriggerDataEnvelope envelope = trigger.getTriggerEnvelope();
        envelope.setData(chainEvent.getExternalData());
        externalEvent.setContext(chainEvent.getContext());
        externalEvent.setMessage(trigger.getTriggerMessage());
        externalEvent.setData(mapper.writeValueAsString(envelope));

        publisher.publishEvent(externalEvent);

        try (ScheduledExecutorService service = Executors.newScheduledThreadPool(2)) {
            service.schedule(()->{
                if (trigger.hasApproval()){

                    try {
                        pushProcess(trigger, chainEvent, true);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 500, TimeUnit.MILLISECONDS);

            service.schedule(()->{
                // ПРОВЕРЯЕМ, НЕ БЫЛ ЛИ ТРИГГЕР ОСТАНОВЛЕН РАНЕЕ
                if (trigger.isActive()){
                    try {
                        pushProcess(trigger, chainEvent, trigger.hasApproval());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 5000, TimeUnit.MILLISECONDS);
        }







    }



    private void pushProcess(Trigger trigger, DeclarativeChainEvent<?,?,?> chainEvent,  boolean pushNext) throws Exception{
        trigger.stop();



        // todo в некоторых случаях необходимо активировать дополнительные действия с data
        if (trigger instanceof YesOrNotTrigger){

            // успешный сценарий для push next
            if (pushNext){
                trigger.onApprove(chainEvent.getInternalData()); // todo не меняется. оставляем для демонстрации

                transaction.execute(status -> {

                    Optional<OutboxEvent> outboxEvent = outboxEventRepository
                            .findByStatusAndCorrelationIdForUpdate(OutboxEvent.OutboxEventStatus.WAITING_FOR_EXTERNAL,
                                    trigger.getCorrelationId()
                                    );

                    outboxEvent.ifPresent(event -> {
                        event.setStatus(OutboxEvent.OutboxEventStatus.WAITING);
                        event.setLast_update(Instant.now());

                    });

                    return null;

                });
            }
            else {
                // disapprove означает прекращение процесса,
                // об этом следует помнить при заполнении стратегии по тому как оценивать успешность опроса
                chainEvent.getInternalData().setCompensationPhase(true);
                chainEvent.setMessage("не получено одобрение на стадии: "+chainEvent.getMessage());
                String payload = mapper.writeValueAsString(chainEvent);

                transaction.execute(status -> {

                    Optional<OutboxEvent> outboxEvent = outboxEventRepository
                            .findByStatusAndCorrelationIdForUpdate(OutboxEvent.OutboxEventStatus.WAITING_FOR_EXTERNAL,
                                    trigger.getCorrelationId()
                            );

                    outboxEvent.ifPresent(event -> {
                        event.setStatus(OutboxEvent.OutboxEventStatus.WAITING);
                        event.setLast_update(Instant.now());
                        event.setPayload(payload);

                    });

                    return null;

                });
            }
        }


    }



    // вызывается с внешнего сервиса
    public void provideAnswer(UserTriggerAnswer answer){
        Trigger trigger = getTrigger(answer.getCorrelationId());
        if (trigger!=null){
            trigger.consumeAnswer(answer);
        }
    }

    public Trigger getTrigger(UUID correlationId){
        return triggers.get(correlationId);
    }



    @Scheduled(fixedRate = 1000*60*60)
    public void clearExpiredTriggers(){
        Instant now = Instant.now();
        triggers.entrySet().removeIf(entry-> entry.getValue().getExpiredAt().isBefore(now));
    }


}
