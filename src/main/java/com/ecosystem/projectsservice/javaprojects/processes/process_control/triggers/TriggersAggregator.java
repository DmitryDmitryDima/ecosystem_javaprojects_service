package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.EventStatus;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;


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











    private final ConcurrentHashMap<UUID, Trigger> unitedTriggerStore = new ConcurrentHashMap<>();

    // регистрация триггера - пользователь должен сделать это сам
    public void registerTrigger(Trigger trigger){
        unitedTriggerStore.put(trigger.getCorrelationId(), trigger);
    }

    // получаем ответ для триггера
    public void feedTrigger(TriggerAnswer answer) throws Exception {
        Trigger trigger = unitedTriggerStore.get(answer.getCorrelationId());
        if (trigger!=null){
            if (trigger instanceof ReactiveTrigger reactiveTrigger){
                synchronized (trigger){
                    reactiveTrigger.registerAnswer(answer);
                    boolean isApproved = reactiveTrigger.isApproved();
                    if (isApproved){
                        System.out.println("ответ принят и триггер закрыт - цепочка движется дальше");
                        pushProcess(reactiveTrigger.getChainEvent(), reactiveTrigger);
                    }
                }
            }
            else {
                trigger.registerAnswer(answer);
            }


        }
    }

    // в зависимости от вида триггера мы производим некоторые действия по контролю его данных и жизненного цикла
    public void initiateTriggerIfExists(DeclarativeChainEvent<?,?,?> chainEvent,
                                        ExternalEvent externalEvent,
                                        ExternalEventType externalEventType, Long waitingFor) throws Exception{

        Trigger trigger = unitedTriggerStore.get(chainEvent.getContext().getCorrelationId());
        // пользователь не создал какого либо триггера
        if (trigger==null) return;

        trigger.setExpirationTime(Instant.now().plusMillis(waitingFor).plusSeconds(60)); // 60 секунд после истекания waiting for

        // отправляем внешний polling ивент процесса (по умолчанию)
        if (trigger.isNeedPollingMessage()){
            externalEvent.setStatus(EventStatus.POLLING);
            externalEvent.setType(externalEventType.getName());


            externalEvent.setData(mapper.writeValueAsString(trigger.getTriggerExternalData()));
            externalEvent.setMessage(trigger.getMessage());
            externalEvent.setContext(chainEvent.getContext());

            // пкбликуем ивент - имея контекст и предустановленный процессом тип, он отправится туда же, куда и основные ивенты процесса
            publisher.publishEvent(externalEvent);
        }
        // реактивный триггер должен хранить данные ивента,
        // так как его обработка происходит при каждом внешнем ответе и не привязана к текущему потоку
        if (trigger instanceof ReactiveTrigger reactiveTrigger){
            reactiveTrigger.setChainEvent(chainEvent);
        }

        else if (trigger instanceof PhaseTrigger phaseTrigger){
            try (ScheduledExecutorService service = Executors.newScheduledThreadPool(2)){
                PhaseStrategy strategy = phaseTrigger.getPhaseStrategy();
                if (strategy == null){
                    throw new IllegalStateException("отсутствуют фазы. Исправьте логику создания триггера");
                }

                List<ScheduledFuture<?>> tasks = new CopyOnWriteArrayList<>();
                long time = 0;

                for (int i = 0; i<strategy.getActions().size(); i++){
                    PhaseStrategy.Phase phase = strategy.getActions().get(i);
                    time+=phase.getPeriod(); // аккумулируем время
                    int stepNum = i; // effectively final

                    tasks.add(
                      service.schedule(()->{


                          if (!phaseTrigger.isWaiting()){
                              tasks.forEach(task->{
                                  task.cancel(false);
                              });
                              return;
                          }
                          // выполняем фазу
                          boolean result = phase.getAction().apply(trigger.getAnswers());
                          // если последний шаг или true на фазе - пушим. Изменения в data вносятся внутри очереди
                          if (strategy.isLast(stepNum) || result){
                              try {
                                  pushProcess(chainEvent, trigger);
                              } catch (Exception e) {
                                  throw new RuntimeException(e);
                              }
                          }


                      }, time, TimeUnit.MILLISECONDS)

                    );
                }
            }
        }


    }









    private void pushProcess(DeclarativeChainEvent<?,?,?> chainEvent, Trigger trigger) throws Exception {
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










    // очищаем просроченные триггеры, на всякий случай останавливаем
    @Scheduled(fixedRate = 1000*60*60)
    public void clearAndStopTriggers(){
        Instant now = Instant.now();
        unitedTriggerStore.entrySet().removeIf(entry-> {

            boolean expired = entry.getValue().getExpirationTime().isBefore(now);
            if (expired){
                entry.getValue().stop();
                return true;
            }
            return false;


        });
    }


}
