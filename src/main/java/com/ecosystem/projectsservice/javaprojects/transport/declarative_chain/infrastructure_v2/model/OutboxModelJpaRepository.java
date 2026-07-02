package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model;


import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// jpa реализация репозитория
@Repository
public interface OutboxModelJpaRepository extends JpaRepository<OutboxModelJpaEntity, UUID> {

    List<OutboxEvent> findByStatus(OutboxEvent.OutboxEventStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT event FROM OutboxEvent event WHERE event.status = :status AND event.correlationId = :correlationId")
    Optional<OutboxEvent>
    findByStatusAndCorrelationIdForUpdate(OutboxEvent.OutboxEventStatus status,
                                          UUID correlationId);
}
