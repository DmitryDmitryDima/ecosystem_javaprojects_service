package com.ecosystem.projectsservice.javaprojects.processes;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

@Service
public class OutboxListener {

    @Autowired
    private InternalEventsManager eventsManager;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;




    @Scheduled(fixedDelay = 500)
    public void readOutbox(){
        List<OutboxEvent> events = transactionTemplate.execute((status)->{
            List<OutboxEvent> currentEvents = outboxEventRepository.findByStatus(OutboxEvent.OutboxEventStatus.WAITING);
            currentEvents.forEach(event->{
                event.setStatus(OutboxEvent.OutboxEventStatus.PROCESSING);
                event.setLast_update(Instant.now());
            });
            // чтение и смена статуса в одной транзакции
            return currentEvents;
        });

        // транзакция закрывается тут. Далее сущности считаются detached
        events.forEach(eventsManager::serializeAndPublish);


    }



}
