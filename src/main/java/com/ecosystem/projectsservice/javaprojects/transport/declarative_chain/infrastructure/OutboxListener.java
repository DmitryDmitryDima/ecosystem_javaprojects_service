package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OutboxListener {


    private static class WaitingEventsTransactionResult{
        List<OutboxEvent> processingEvents = new ArrayList<>();
        List<OutboxEvent> outdatedForReadEvents = new ArrayList<>(); // сценарий, когда по какой-то причине просрочился ивент с waiting статусом
        // в данном случае надо быть очень аккуратным с механизмом компенсаций, чтобы не стереть то, что было внесено (возможно) другим процессом
    }



    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ChainManager chainManager;

    // todo добавить обработку ивентов, устаревших при статусе waiting (очень редкий кейс)
    @Scheduled(fixedDelay = 500)
    public void readWaitingOutbox(){

        WaitingEventsTransactionResult result = transactionTemplate.execute(status -> {
            List<OutboxEvent> waitingEvents = outboxEventRepository.findByStatus(OutboxEvent.OutboxEventStatus.WAITING);
            WaitingEventsTransactionResult allTransactionsResult = new WaitingEventsTransactionResult();
            waitingEvents.forEach(outboxEvent -> {
                Instant now = Instant.now();
                Instant readExpiration = outboxEvent.getReadExpiration();
                outboxEvent.setStatus(OutboxEvent.OutboxEventStatus.PROCESSING);
                outboxEvent.setLast_update(Instant.now()); // важно - каждая смена статуса фиксируется. Это - точка отсчета длительности выполнения

                if (readExpiration==null || readExpiration.isAfter(now)){

                    allTransactionsResult.processingEvents.add(outboxEvent);
                }

                else {
                   allTransactionsResult.outdatedForReadEvents.add(outboxEvent);
                }







            });
            return allTransactionsResult;
        });

        result.processingEvents.forEach(chainManager::processWaitingEvents);
        result.outdatedForReadEvents.forEach(chainManager::processExpiredWaitingEvents);


    }
    // обработка ивентов, устаревших со статусом processing
    // todo добавить обновление времени updated_at, чтобы не было повторного прочтения
    @Scheduled(fixedDelay = 1000)
    public void readExpiredProcessingOutboxEvents(){


        List<OutboxEvent> expiredEvents = transactionTemplate.execute(status ->{
            List<OutboxEvent> expiredProcessing  = outboxEventRepository
                    .findByStatus(OutboxEvent.OutboxEventStatus.PROCESSING).stream()
                    .filter(event->{
                        if (event.getReadExpiration()==null && event.getPerformanceExpirationPeriod()==null){
                            // todo - что делать, если сообщение зависло в processing?
                            return false;
                        }


                        Instant expirationTime = event.getLast_update().plusMillis(event.getPerformanceExpirationPeriod());
                        boolean isExpired = Instant.now().isAfter(expirationTime);

                        // если просрочено, то меняем last update во избежание повторного прочитывания
                        if (isExpired){
                            event.setLast_update(Instant.now());
                        }
                        return isExpired;




                        })
                    .toList();



            return expiredProcessing;
        });
        expiredEvents.forEach(chainManager::processExpiredProcessingEvent);


    }

    // обработка просроченных ивентов с waiting_for
    @Scheduled(fixedDelay = 1000)
    public void readExpiredWaitingForOutboxEvents(){

        List<OutboxEvent> toProcess = transactionTemplate.execute(status -> {
            List<OutboxEvent> expiredEvents = outboxEventRepository.findByStatus(OutboxEvent.OutboxEventStatus.WAITING_FOR_EXTERNAL).stream()
                    .filter(outboxEvent -> {
                                if (outboxEvent.getReadExpiration()!=null){
                                    return outboxEvent.getReadExpiration().isBefore(Instant.now());
                                }
                                return false;

                            }
                    ).toList();

            expiredEvents.forEach(outboxEvent -> {
                outboxEvent.setStatus(OutboxEvent.OutboxEventStatus.PROCESSING);
                outboxEvent.setLast_update(Instant.now());
            });


            return expiredEvents;
        });

        toProcess.forEach(chainManager::processExpiredWaitingForEvent);




    }


}
