package com.ecosystem.projectsservice.javaprojects.processes.chains;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.processes.InternalEventsManager;
import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.BasicQueueEvent;
import com.ecosystem.projectsservice.javaprojects.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public abstract class AbstractOutboxChain <C extends CompensationEvent, E extends ChainEntranceInfo>{


    // информация о цепочке
    @Autowired
    private InternalEventsManager eventsManager;

    @Autowired
    private ObjectMapper objectMapper;

    // сюда мы пишем каждый следующий ивент
    @Autowired
    private OutboxEventRepository outboxRepository;






    public abstract void init(SecurityContext securityContext, RequestContext requestContext, E info) throws Exception;



    public abstract void compensation(C compensationEvent);

    // внутренние ивенты-участники цепочки
    public abstract List<Class<? extends ChainEvent>> getAllSteps();

    // название ивента - результата всей цепочки. Ивент может иметь статусы успеха, ошибки или в процессе
    public abstract String getResultingEventName();


    @PostConstruct
    public void registerEvents(){
        eventsManager.registerEvents(getAllSteps());
    }


    public ObjectMapper mapper(){return objectMapper;}

    public OutboxEventRepository outbox(){return outboxRepository;}

    public InternalEventsManager eventsManager(){return eventsManager;}

    // если существует ретрай механизм - делаем ретрай, если нет - генерируем компенсацию
    public void errorProcessing(ChainEvent failedEvent,
                                ChainEvent compensationEvent,
                                BasicQueueEvent queueEvent) throws Exception {
        Retryable retryable = failedEvent.getClass().getAnnotation(Retryable.class);

        // если ретрай не предусмотрен, запускаем компенсацию и отправляем failed event
        if (retryable==null){
            sendResult(queueEvent, failedEvent.getOutboxParent());
            pushChain(compensationEvent, failedEvent.getOutboxParent());
        }
        else {
            long count = retryable.count();
            if (count>=failedEvent.getCurrentRetry()){
                pushChain(compensationEvent, failedEvent.getOutboxParent());
            }
            else {
                // пытаемся снова войти в ивент
                failedEvent.setCurrentRetry(failedEvent.getCurrentRetry()+1);
                pushChain(failedEvent, failedEvent.getOutboxParent());
            }

        }
    }

    // публикация в таблицу результирующего ивента
    public void sendResult(BasicQueueEvent queueEvent, Long previousOutbox) throws Exception {
        pushChain(queueEvent, previousOutbox);
    };

    public abstract ChainEvent generateResultingEvent();

    // в данном методе мы создаем новую запись в outbox таблицу, при этом проставляя статус в предыдущей записи
    @Transactional(Transactional.TxType.REQUIRED)
    protected void pushChain(ChainEvent newEvent, Long previous) throws Exception{
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .type(newEvent.getClass().getAnnotation(EventName.class).value())
                .payload(objectMapper.writeValueAsString(newEvent))
                .status(OutboxEvent.OutboxEventStatus.WAITING)
                .last_update(Instant.now())
                .build();

        outboxRepository.save(outboxEvent);

        // callback
        if (previous!=null){
            Optional<OutboxEvent> previousEventCheck = outboxRepository.findById(previous);
            // todo как обработать ситуацию, когда прошлого ивента не существует?
            if (previousEventCheck.isPresent()){
                OutboxEvent oldEvent = previousEventCheck.get();
                oldEvent.setLast_update(Instant.now());
                oldEvent.setStatus(OutboxEvent.OutboxEventStatus.PROCESSED);
                outboxRepository.save(oldEvent);
            }
        }







}}
