package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.idempotency;


import java.time.Instant;
import java.util.UUID;

// интерфейс модели таблицы идемпотентности
public interface IdempotencyModel {


    // uuid как unique constraint
    UUID getProcessUUID();


}
