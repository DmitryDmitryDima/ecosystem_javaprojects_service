package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.EventStatus;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
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









    private final ConcurrentHashMap <UUID, CustomTrigger> triggersStore = new ConcurrentHashMap<>();


    public void registerTrigger(CustomTrigger customTrigger){
        triggersStore.put(customTrigger.getCorrelationId(), customTrigger);
    }

    public void notifyTrigger(TriggerAnswer answer){
        CustomTrigger trigger = triggersStore.get(answer.getCorrelationId());
        if (trigger!=null){
            trigger.registerAnswer(answer);
        }
    }

    public void initiateTrigger(DeclarativeChainEvent<?,?,?> chainEvent,
                                ExternalEvent externalEvent,
                                ExternalEventType externalEventType, Long waitingFor) throws Exception{
        System.out.println("активируем триггер если он существует");
        CustomTrigger customTrigger = triggersStore.get(chainEvent.getContext().getCorrelationId());
        // если пользователь не создал триггер, то waiting for активируется по-другому или при создании очереди создать его забыли
        if (customTrigger == null){
            return;
        }

        // обновляем время устаревания
        customTrigger.setExpirationTime(Instant.now().plusSeconds(waitingFor).plusSeconds(10));

        externalEvent.setStatus(EventStatus.POLLING);
        externalEvent.setType(externalEventType.getName());


        externalEvent.setData(mapper.writeValueAsString(customTrigger.getTriggerExternalData()));
        externalEvent.setMessage(customTrigger.getMessage());
        externalEvent.setContext(chainEvent.getContext());

        // пкбликуем ивент - имея контекст и предустановленный процессом тип, он отправится туда же, куда и основные ивенты процесса
        publisher.publishEvent(externalEvent);

        // запускаем два обработчика, которые составляют собой двойную проверку триггера

        try (ScheduledExecutorService service = Executors.newScheduledThreadPool(2)){

            service.schedule(()->{
                // запуск стратегии реакции на опрос активности
                boolean waitingResult = customTrigger.isWaitingPhaseApproved();
                if (waitingResult){
                    try {
                        pushOutbox(chainEvent, customTrigger);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }, customTrigger.getActivityPhaseWaitingTimeInMs(), TimeUnit.MILLISECONDS);


            service.schedule(()->{
                // проверяем, не был ли триггер закрыт в первом таймере
                if (customTrigger.isWaiting()){
                    // выполняем стратегию принятия решения после ожидания
                    customTrigger.isDecisionPhaseApproved();
                    try {
                        pushOutbox(chainEvent, customTrigger);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }




            }, customTrigger.getDecisionPhaseWaitingTimeInMs(), TimeUnit.MILLISECONDS);



        }



    }

    private void pushOutbox(DeclarativeChainEvent<?,?,?> chainEvent, CustomTrigger trigger) throws Exception{
        trigger.stop(); // останавливаем триггер для того, чтобы он больше не принимал ответы и не мог быть вновь рассмотрен обработчиком

        // не забываем, что методы триггера могут вносить изменения в internal data, стратегия прописывается создателем процесса
        String payload = mapper.writeValueAsString(chainEvent);

        // активируем outbox, вносим (возможно) новые данные через internal event data
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






    @Scheduled(fixedRate = 1000*60*60)
    public void clearExpiredTriggers(){
        Instant now = Instant.now();
        triggersStore.entrySet().removeIf(entry-> entry.getValue().getExpirationTime().isBefore(now));
    }


}
