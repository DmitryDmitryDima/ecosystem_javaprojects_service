package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure;

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
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ChainManager chainManager;

    @Scheduled(fixedDelay = 500)
    public void readWaitingOutbox(){

        List<OutboxEvent> processingEvents = transactionTemplate.execute(status -> {
            List<OutboxEvent> waitingEvents = outboxEventRepository.findByStatus(OutboxEvent.OutboxEventStatus.WAITING);

            waitingEvents.forEach(outboxEvent -> {
                outboxEvent.setStatus(OutboxEvent.OutboxEventStatus.PROCESSING);
                outboxEvent.setLast_update(Instant.now());
            });
            return waitingEvents;
        });

        processingEvents.forEach(chainManager::deserializeAndPublish);


    }







}
