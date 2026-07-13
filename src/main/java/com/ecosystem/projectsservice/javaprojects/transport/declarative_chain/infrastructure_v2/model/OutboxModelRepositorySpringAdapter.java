package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model;


import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class OutboxModelRepositorySpringAdapter implements OutboxModelRepository {



    private OutboxModelJpaRepository jpaRepository;


    private TransactionTemplate transaction;


    public OutboxModelRepositorySpringAdapter(OutboxModelJpaRepository jpaRepository,
                                              TransactionTemplate transaction) {
        this.jpaRepository = jpaRepository;
        this.transaction = transaction;
    }



    @Override
    public void save(OutboxModel model) {

        try {


            // используя интерфейс, производим маппинг
            OutboxModelJpaEntity entity = OutboxModelJpaEntity.builder()
                    .outboxUUID(model.getOutboxUUID())
                    .processUUID(model.getProcessUUID())
                    .status(model.getStatus())
                    .type(model.getType())
                    .payload(model.getPayload())
                    .lastUpdate(model.getLastUpdate())
                    .readExpiration(model.getReadExpiration())
                    .performanceLimitTime(model.getPerformanceLimitTime())
                    .allReadVersion(model.getAllReadVersion())
                    .allReadProcessingVersion(model.getAllReadProcessingVersion())
                    .build();


            transaction.execute(status -> {

                jpaRepository.save(entity);

                return null;
            });


        }
        catch (Exception e){
            throw
                    new OutboxRepositoryException("outbox model saving error. Reason: "
                            +e.getMessage());
        }

    }

    @Override
    public void markAsProcessed(UUID id) {


        try {

            transaction.execute(status -> {
                Optional<OutboxModelJpaEntity> outboxEventCheck = jpaRepository.findById(id);
                outboxEventCheck.ifPresent(outbox->{
                    outbox.setStatus(OutboxStatus.PROCESSED);
                });

                return null;
            });

        }

        catch (Exception e){
            throw
                    new OutboxRepositoryException("processed callback error. Reason: "
                            +e.getMessage());
        }





    }

    @Override
    public void changeStatusForGivenAllReadVersion(UUID uuid, OutboxStatus toStatus, Long forAllReadVersion) {

    }

    @Override
    public void changeStatusAndMessageForGivenAllReadVersion(UUID uuid, OutboxStatus toStatus, String message, Long forAllReadVersion) {

    }



    @Override
    public List<OutboxModel> readEverlastingProcessingEvents(Long batchSize) {
        return List.of();
    }

    @Override
    public List<OutboxModel> readEverlastingProcessingEvents() {
        return List.of();
    }

    @Override
    public List<OutboxModel> readMissedExpiredProcessingEvents() {
        return List.of();
    }

    @Override
    public List<OutboxModel> readMissedExpiredProcessingEvents(Long batchSize) {
        return List.of();
    }

    @Override
    public List<OutboxModel> readExpiredProcessingEvents() {
        return List.of();
    }

    @Override
    public List<OutboxModel> readExpiredProcessingEvents(Long batchSize) {
        return List.of();
    }

    @Override
    public List<OutboxModel> readActualWaitingEvents() {
        return List.of();
    }

    @Override
    public List<OutboxModel> readActualWaitingEvents(Long batchSize) {
        return List.of();
    }

    @Override
    public List<OutboxModel> readExpiredWaitingEvents() {
        return List.of();
    }

    @Override
    public List<OutboxModel> readExpiredWaitingEvents(Long batchSize) {
        return List.of();
    }

    @Override
    public List<OutboxModel> readExpiredWaitingForSignalEvents() {
        return List.of();
    }

    @Override
    public List<OutboxModel> readExpiredWaitingForSignalEvents(Long batchSize) {
        return List.of();
    }

    @Override
    public List<OutboxModel> readManagerCrashEvents() {
        return List.of();
    }

    @Override
    public List<OutboxModel> readManagerCrashEvents(Long batchSize) {
        return List.of();
    }


}
