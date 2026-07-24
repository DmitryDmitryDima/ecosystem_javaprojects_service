package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model;


import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// jpa реализация репозитория
@Repository
public interface OutboxModelJpaRepository extends JpaRepository<OutboxModelJpaEntity, UUID> {









    // блокирующий поиск сущности

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT entity FROM OutboxModelJpaEntity " +
            "entity where entity.outboxUUID = :uuid")
    Optional<OutboxModelJpaEntity>
    findByUUIDForUpdate(UUID uuid);
    



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT entity FROM OutboxModelJpaEntity " +
            "entity WHERE entity.status = :status AND entity.processUUID = :processId")
    Optional<OutboxModelJpaEntity>
    findByStatusAndCorrelationIdForUpdate(OutboxStatus status,
                                          UUID processId);



    // SKIP LOCKED - чтобы пропустить УЖЕ БЛОКНУТЫЕ СТРОКИ
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT entity FROM OutboxModelJpaEntity entity where entity.status = :status " +
            "and entity.readExpiration>CURRENT_TIMESTAMP ")
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value ="-2")})

    List<OutboxModelJpaEntity>
    readAllEntitiesByStatusWhereReadExpirationNotReached(OutboxStatus status);




    // SKIP LOCKED - ЧТОБЫ ПРОПУСТИТЬ УЖЕ БЛОКНУТЫЕ СТРОКИ
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT entity FROM OutboxModelJpaEntity entity where entity.status = :status " +
            "and entity.readExpiration<CURRENT_TIMESTAMP")
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value ="-2")})

    List<OutboxModelJpaEntity> readAllEntitiesWithReadExpirationReached(OutboxStatus status);






}
