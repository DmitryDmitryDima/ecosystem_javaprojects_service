package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.id.uuid.UuidVersion7Strategy;

import java.time.Instant;
import java.util.UUID;

// Jpa реализация
@Entity
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "outbox_model")
public class OutboxModelJpaEntity implements OutboxModel {

    @Id
    @GeneratedValue
    @UuidGenerator(algorithm = UuidVersion7Strategy.class)
    private UUID outboxUUID;



    private UUID processUUID;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private String type;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private Instant lastUpdate;

    private Instant readExpiration;

    private Long performanceLimitTime;

    private Long readVersion;



    @Override
    public UUID getOutboxUUID() {
        return outboxUUID;
    }

    @Override
    public UUID getProcessUUID() {
        return processUUID;
    }

    @Override
    public OutboxStatus getStatus() {
        return status;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getPayload() {
        return payload;
    }

    @Override
    public Instant getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public Instant getReadExpiration() {
        return readExpiration;
    }

    @Override
    public Long getPerformanceLimitTime() {
        return performanceLimitTime;
    }

    @Override
    public Long getReadVersion() {
        return 0L;
    }
}
