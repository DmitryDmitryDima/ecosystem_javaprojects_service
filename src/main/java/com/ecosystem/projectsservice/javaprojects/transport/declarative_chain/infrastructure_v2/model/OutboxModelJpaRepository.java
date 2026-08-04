package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model;


import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.*;
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


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT entity FROM OutboxModelJpaEntity entity where entity.status = :status")
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value ="-2")})

    List<OutboxModelJpaEntity> readByStatus(OutboxStatus status);



    // TODO пока используем нативный запрос, нужно разобраться, как это абстрагировать для hql
    @NativeQuery("select * from  outbox_model entity  where entity.status = 'PROCESSING' " +
            "and entity.all_read_processing_version = 0 and entity.performance_limit_time is not null " +
            "and entity.last_update + (entity.performance_limit_time * interval '1 ms' )<now() for update skip locked")
    List<OutboxModelJpaEntity> readEventsExpiredByPerformance();

    @NativeQuery("select * from  outbox_model entity  where entity.status = 'PROCESSING' " +
            "and entity.all_read_processing_version >0  and entity.performance_limit_time is not null " +
            "and entity.last_update + (entity.performance_limit_time * interval '1 ms' )<now() for update skip locked")
    List<OutboxModelJpaEntity> readMissedEventsExpiredByPerformance();










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



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT entity FROM OutboxModelJpaEntity entity " +
            "where entity.performanceLimitTime is null and entity.status = :status")
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value ="-2")})

    List<OutboxModelJpaEntity> readEverlastingSteps(OutboxStatus status);











}
