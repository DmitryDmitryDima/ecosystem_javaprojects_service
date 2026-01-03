package com.ecosystem.projectsservice.javaprojects.processes.chains;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.processes.InternalEventsManager;
import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.BasicQueueEvent;
import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.ExternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.EventStatus;
import com.ecosystem.projectsservice.javaprojects.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Slf4j
public abstract class AbstractOutboxChain
        <
        E extends ChainEntranceInfo,
        C extends CompensationEvent,
        R extends BasicQueueEvent
        >
{


    // информация о цепочке
    @Autowired
    private InternalEventsManager eventsManager;

    @Autowired
    private ObjectMapper objectMapper;

    // сюда мы пишем каждый следующий ивент
    @Autowired
    private OutboxEventRepository outboxRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;


    // в цепочках мы сознательно отказываемся от декларативных транзакций для обеспечения максимальной гибкости
    public TransactionTemplate transaction(){return transactionTemplate;}






    public abstract void init(SecurityContext securityContext, RequestContext requestContext, E info) throws Exception;


    // компенсация не может быть retryable, при ошибке в ней должна быть особая обработка
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


    // данный метод оперирует всеми возможными данными для всех возможных сценариев. Среди них - компенсация, ивент, который нужно повторить при retry, ивент для внешней очереди
    public void errorProcessing(ChainEvent failedEvent,
                                ChainEvent compensationEvent,
                                R queueEvent)  {

        try {
            log.info("Error occured after event "+failedEvent.getClass().getName()+" with parent "+failedEvent.getOutboxParent());
            log.info("Reason: "+queueEvent.getMessage());
            Retryable retryable = failedEvent.getClass().getAnnotation(Retryable.class);

            // если ретрай не предусмотрен, запускаем компенсацию и отправляем failed event
            if (retryable==null){
                log.info("this event is not retryable, sending result for external queue and publish compensation");
                pushChainWithExternalMessage(compensationEvent, queueEvent, failedEvent.getOutboxParent());
            }
            else {
                log.info("this event is retryable");
                long count = retryable.count();
                log.info("count is equal "+count);
                if (count<=failedEvent.getCurrentRetry()){
                    log.info("count exceeded");
                    pushChainWithExternalMessage(compensationEvent, queueEvent, failedEvent.getOutboxParent());
                }
                else {
                    // пытаемся снова войти в ивент
                    log.info("repeat event");
                    failedEvent.setCurrentRetry(failedEvent.getCurrentRetry()+1);
                    pushChain(failedEvent, failedEvent.getOutboxParent());
                }

            }
        }
        catch (Exception e){
            // todo какие сценарии доступны, если падает сам этап лечения?
        }


    }




    public abstract R generateResult(String message, EventStatus status, ExternalEventContext context, ExternalEventData data);




    // всегда явно заканчиваем цепочку посредством генерации результирующего ивента
    public void sendFinalResult(R messageEvent, Long previousOutbox) throws Exception{
        OutboxEvent outboxEventForMessage = buildOutboxEvent(messageEvent);
        try {
            transaction().execute((status -> {






                outboxRepository.save(outboxEventForMessage);
                // callback
                if (previousOutbox!=null){
                    Optional<OutboxEvent> previousEventCheck = outboxRepository.findById(previousOutbox);
                    // todo нужно ли обработать ситуацию, когда прошлого ивента не существует даже при известном id
                    if (previousEventCheck.isPresent()){
                        OutboxEvent oldEvent = previousEventCheck.get();
                        oldEvent.setLast_update(Instant.now());
                        oldEvent.setStatus(OutboxEvent.OutboxEventStatus.PROCESSED);
                        outboxRepository.save(oldEvent);
                    }
                }
                return null;
            }));
        }

        catch (Exception pushingError){
            log.info(pushingError.getMessage());
            // todo сценарий, когда публикация следующего шага провалилась - нужна ли отдельная обработка?
        }
    }




    // если нам нужно отправить сообщение вместе с переходом к следующему шагу, мы используем этот метод явно
    public void pushChainWithExternalMessage(ChainEvent newEvent, R messageEvent, Long previousOutbox) throws Exception{
        OutboxEvent outboxEventForNewEvent = buildOutboxEvent(newEvent);
        OutboxEvent outboxEventForMessage = buildOutboxEvent(messageEvent);

        try {
            transaction().execute((status -> {





                outboxRepository.save(outboxEventForNewEvent);
                outboxRepository.save(outboxEventForMessage);
                // callback
                if (previousOutbox!=null){
                    Optional<OutboxEvent> previousEventCheck = outboxRepository.findById(previousOutbox);
                    // todo нужно ли обработать ситуацию, когда прошлого ивента не существует даже при известном id
                    if (previousEventCheck.isPresent()){
                        OutboxEvent oldEvent = previousEventCheck.get();
                        oldEvent.setLast_update(Instant.now());
                        oldEvent.setStatus(OutboxEvent.OutboxEventStatus.PROCESSED);
                        outboxRepository.save(oldEvent);
                    }
                }
                return null;
            }));
        }

        catch (Exception pushingError){
            log.info(pushingError.getMessage());
            // todo сценарий, когда публикация следующего шага провалилась - нужна ли отдельная обработка?
        }

    }

    private OutboxEvent buildOutboxEvent(ChainEvent chainEvent) throws Exception{
        return OutboxEvent.builder()
                .type(chainEvent.getClass().getAnnotation(EventName.class).value())
                .payload(objectMapper.writeValueAsString(chainEvent))
                .status(OutboxEvent.OutboxEventStatus.WAITING)
                .last_update(Instant.now())
                .build();
    }



    public void pushChain(ChainEvent newEvent, Long previousOutbox) throws Exception{


        // атомарные операции -  коллбэк для предыдущего ивента + создание новой записи для следующего ивента или компенсации

        OutboxEvent outboxEvent = buildOutboxEvent(newEvent);

        try {
            transaction().execute((status -> {





                outboxRepository.save(outboxEvent);
                // callback
                if (previousOutbox!=null){
                    Optional<OutboxEvent> previousEventCheck = outboxRepository.findById(previousOutbox);
                    // todo нужно ли обработать ситуацию, когда прошлого ивента не существует даже при известном id
                    if (previousEventCheck.isPresent()){
                        OutboxEvent oldEvent = previousEventCheck.get();
                        oldEvent.setLast_update(Instant.now());
                        oldEvent.setStatus(OutboxEvent.OutboxEventStatus.PROCESSED);
                        outboxRepository.save(oldEvent);
                    }
                }
                return null;
            }));
        }

        catch (Exception pushingError){
            log.info(pushingError.getMessage());
            // todo сценарий, когда публикация следующего шага провалилась - нужна ли отдельная обработка?
        }












}}
