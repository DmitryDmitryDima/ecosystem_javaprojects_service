package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model;


import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
public class OutboxModelRepositorySpringAdapter implements OutboxModelRepository {


    @Autowired
    private OutboxModelJpaRepository jpaRepository;

    @Autowired
    private TransactionTemplate transaction;




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
                    .build();


            transaction.execute(status -> {

                jpaRepository.save(entity);

                return null;
            });

            jpaRepository.save(entity);
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
}
