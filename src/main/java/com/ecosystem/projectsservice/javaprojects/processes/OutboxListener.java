package com.ecosystem.projectsservice.javaprojects.processes;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class OutboxListener {

    @Autowired
    private InternalEventsManager eventsManager;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;




    @Scheduled(fixedDelay = 500)
    public void readWaitingOutbox(){
        List<OutboxEvent> events = transactionTemplate.execute((status)->{
            List<OutboxEvent> currentEvents = outboxEventRepository.findByStatus(OutboxEvent.OutboxEventStatus.WAITING);
            currentEvents.forEach(event->{


                log.info(event.getStatus()+" performed read from outbox "+event.getType()+" with id "+event.getId());
                event.setStatus(OutboxEvent.OutboxEventStatus.PROCESSING);
                event.setLast_update(Instant.now());
            });
            // чтение и смена статуса в одной транзакции
            return currentEvents;
        });

        // транзакция закрывается тут. Далее сущности считаются detached
        events.forEach(eventsManager::serializeAndPublish);


    }

    // если ивент относится к внешнему сообщению - он проставляется автоматически
    @Scheduled(fixedDelay = 2000)
    public void readProcessingOutbox(){
        transactionTemplate.execute(status -> {

            List<OutboxEvent> currentEvents = outboxEventRepository.findByStatus(OutboxEvent.OutboxEventStatus.PROCESSING);
            for (OutboxEvent event:currentEvents){
                if (eventsManager.isBridgeEvent(event.getType())){
                    event.setStatus(OutboxEvent.OutboxEventStatus.PROCESSED);
                }
            }
            return null;
        });
    }



}
