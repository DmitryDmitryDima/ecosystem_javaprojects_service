package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.idempotency;


import java.time.Instant;
import java.util.UUID;

// интерфейс модели таблицы идемпотентности
public interface IdempotencyModel {


    // uuid как unique constraint
    UUID getProcessUUID();


}
