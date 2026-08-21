package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.model.outbox;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModel;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxStatus;
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
@ToString
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

    private Long allReadVersion = 0L;

    private Long allReadProcessingVersion = 0L;

    private String message;

    private boolean compensation;

    private Instant lockedUntil;





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
    public Long getAllReadVersion() {
        return allReadVersion;
    }

    @Override
    public Long getAllReadProcessingVersion() {
        return allReadProcessingVersion;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isCompensation() {
        return compensation;
    }

    @Override
    public Instant getLockedUntil() {
        return lockedUntil;
    }


}
