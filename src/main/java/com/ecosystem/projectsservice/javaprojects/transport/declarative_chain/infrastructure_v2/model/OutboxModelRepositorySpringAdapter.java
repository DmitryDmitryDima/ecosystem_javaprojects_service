package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model;


import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
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





    // создание новой записи
    @Override
    public void create(OutboxModel model) {

        try {


            // используя интерфейс, производим маппинг
            OutboxModelJpaEntity entity = OutboxModelJpaEntity.builder()


                    .processUUID(model.getProcessUUID())
                    .status(model.getStatus())
                    .type(model.getType())
                    .payload(model.getPayload())
                    .lastUpdate(model.getLastUpdate())
                    .readExpiration(model.getReadExpiration())
                    .performanceLimitTime(model.getPerformanceLimitTime())
                    .allReadVersion(0L)
                    .allReadProcessingVersion(0L)
                    .message(model.getMessage())
                    .build();


            transaction.execute(status -> {

                jpaRepository.save(entity);

                return null;
            });


        }
        catch (Exception e){
            throw
                    new OutboxRepositoryException("Не удалось создать outbox модель, причина: "
                            +e.getMessage());
        }

    }

    @Override
    public void markPreviousAsProcessedAndCreateNewModel(UUID previous, OutboxModel model) {

    }

    // todo каким должен быть такой запрос?
    /*
    должен ли он гарантировать, что предыдущий статус - processing, и compensation = false
    Так мы защищаем от ситуации,
     когда успешная цепь пытается продолжиться после входа в компенсационный режим
     */

    @Override
    public void markAsProcessedForSuccessStep(UUID id) {


        try {



        }

        catch (Exception e){
            throw
                    new OutboxRepositoryException("processed callback error. Reason: "
                            +e.getMessage());
        }





    }

    @Override
    public void markAsProcessedForCompensation(UUID id) {

    }

    @Override
    public void markAsCompensating(UUID id) {

    }

    @Override
    public void changeStatusForGivenAllReadVersion(UUID uuid, OutboxStatus toStatus, Long forAllReadVersion) {

    }

    @Override
    public void changeStatusAndMessageForGivenAllReadVersion(UUID uuid, OutboxStatus toStatus, String message, Long forAllReadVersion) {

    }






    // обновляем readVersion, атомарно меняем статус на processing
    @Override
    public List<? extends OutboxModel> readActualWaitingEvents() {

        try {
            return
                    transaction.execute(status -> {

                    List<OutboxModelJpaEntity> jpaEntities = jpaRepository
                        .readAllEntitiesByStatusWhereReadExpirationNotReached(OutboxStatus.WAITING);


                    // при чтении статус меняется на processing,
                    // обновляется last update,
                    // а также происходит обновление readVersion
                    jpaEntities.forEach(outboxModelJpaEntity -> {
                        outboxModelJpaEntity.setLastUpdate(Instant.now());
                        outboxModelJpaEntity.setAllReadVersion(outboxModelJpaEntity.getAllReadVersion()+1);
                        outboxModelJpaEntity.setStatus(OutboxStatus.PROCESSING);

                    });

                return jpaEntities;}
                    );
        }

        catch (Exception e){
            throw new
                    OutboxRepositoryException("Не удалось получить актуальные Waiting записи. Причина: "+e.getMessage());
        }
    }

    @Override
    public List<? extends OutboxModel> readActualWaitingEvents(Long batchSize) {
        return List.of();
    }


    @Override
    public List<? extends OutboxModel> readEverlastingProcessingEvents(Long batchSize) {
        return List.of();
    }

    @Override
    public List<? extends OutboxModel> readEverlastingProcessingEvents() {
        return List.of();
    }

    @Override
    public List<? extends OutboxModel> readMissedExpiredProcessingEvents() {
        return List.of();
    }

    @Override
    public List<? extends OutboxModel> readMissedExpiredProcessingEvents(Long batchSize) {
        return List.of();
    }

    @Override
    public List<? extends OutboxModel> readExpiredProcessingEvents() {
        return List.of();
    }

    @Override
    public List<? extends OutboxModel> readExpiredProcessingEvents(Long batchSize) {
        return List.of();
    }

    @Override
    public List<? extends OutboxModel> readExpiredWaitingEvents() {

        try {
            return
                    transaction.execute(status -> {

                        List<OutboxModelJpaEntity> jpaEntities = jpaRepository
                                .readAllEntitiesWithReadExpirationReached(OutboxStatus.WAITING);


                        // при чтении статус меняется на processing,
                        // обновляется last update,
                        // а также происходит обновление readVersion
                        jpaEntities.forEach(outboxModelJpaEntity -> {
                            outboxModelJpaEntity.setLastUpdate(Instant.now());
                            outboxModelJpaEntity.setAllReadVersion(outboxModelJpaEntity.getAllReadVersion()+1);
                            outboxModelJpaEntity.setStatus(OutboxStatus.PROCESSING);

                        });

                        return jpaEntities;}
                    );
        }

        catch (Exception e){
            throw new
                    OutboxRepositoryException("Не удалось получить просроченные Waiting записи. Причина: " +
                    ""+e.getMessage());
        }


    }

    @Override
    public List<? extends OutboxModel> readExpiredWaitingEvents(Long batchSize) {
        return List.of();
    }

    @Override
    public List<? extends OutboxModel> readExpiredWaitingForSignalEvents() {


        try {
            return
                    transaction.execute(status -> {

                        List<OutboxModelJpaEntity> jpaEntities = jpaRepository
                                .readAllEntitiesWithReadExpirationReached(OutboxStatus.WAITING_FOR_EXTERNAL);


                        // при чтении статус меняется на processing,
                        // обновляется last update,
                        // а также происходит обновление readVersion
                        jpaEntities.forEach(outboxModelJpaEntity -> {
                            outboxModelJpaEntity.setLastUpdate(Instant.now());
                            outboxModelJpaEntity.setAllReadVersion(outboxModelJpaEntity.getAllReadVersion()+1);
                            outboxModelJpaEntity.setStatus(OutboxStatus.PROCESSING);

                        });

                        return jpaEntities;}
                    );
        }

        catch (Exception e){
            throw new
                    OutboxRepositoryException("Не удалось получить просроченные" +
                    " Waiting for External записи. Причина: " +
                    ""+e.getMessage());
        }

    }

    @Override
    public List<? extends OutboxModel> readExpiredWaitingForSignalEvents(Long batchSize) {
        return List.of();
    }

    @Override
    public List<? extends OutboxModel> readManagerCrashEvents() {
        return List.of();
    }

    @Override
    public List<? extends OutboxModel> readManagerCrashEvents(Long batchSize) {
        return List.of();
    }
}
