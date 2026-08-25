package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.model.outbox;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output.ChainOutput;
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


    // поля используются при сохранении waiting for signal ивента
    private Long readLockPeriod;

    // поля используются при сохранении waiting for signal ивента
    private Long readExpirationPeriod;





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

    @Override
    public Long getReadLockPeriod() {
        return readLockPeriod;
    }

    @Override
    public Long getReadExpirationPeriod() {
        return readExpirationPeriod;
    }


    @Override
    public String toString(){

        return "outbox entity with "+outboxUUID+" related to process with "+processUUID;
    }





    public static OutboxModelJpaEntityBuilder builder() {
        return new OutboxModelJpaEntityBuilder();
    }


    public void setReadExpirationPeriod(Long readExpirationPeriod) {
        this.readExpirationPeriod = readExpirationPeriod;
    }

    public void setReadLockPeriod(Long readLockPeriod) {
        this.readLockPeriod = readLockPeriod;
    }

    public void setOutboxUUID(final UUID outboxUUID) {
        this.outboxUUID = outboxUUID;
    }


    public void setProcessUUID(final UUID processUUID) {
        this.processUUID = processUUID;
    }


    public void setStatus(final OutboxStatus status) {
        this.status = status;
    }


    public void setType(final String type) {
        this.type = type;
    }


    public void setPayload(final String payload) {
        this.payload = payload;
    }


    public void setLastUpdate(final Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }


    public void setReadExpiration(final Instant readExpiration) {
        this.readExpiration = readExpiration;
    }


    public void setPerformanceLimitTime(final Long performanceLimitTime) {
        this.performanceLimitTime = performanceLimitTime;
    }


    public void setAllReadVersion(final Long allReadVersion) {
        this.allReadVersion = allReadVersion;
    }


    public void setAllReadProcessingVersion(final Long allReadProcessingVersion) {
        this.allReadProcessingVersion = allReadProcessingVersion;
    }


    public void setMessage(final String message) {
        this.message = message;
    }


    public void setCompensation(final boolean compensation) {
        this.compensation = compensation;
    }


    public void setLockedUntil(final Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }


    public OutboxModelJpaEntity() {
    }


    public OutboxModelJpaEntity(final UUID outboxUUID,
                                final UUID processUUID,
                                final OutboxStatus status,
                                final String type,
                                final String payload,
                                final Instant lastUpdate,
                                final Instant readExpiration,
                                final Long performanceLimitTime,
                                final Long allReadVersion,
                                final Long allReadProcessingVersion,
                                final String message,
                                final boolean compensation,
                                final Instant lockedUntil,
                                final Long readLockPeriod,
                                final Long readExpirationPeriod
                                ) {
        this.outboxUUID = outboxUUID;
        this.processUUID = processUUID;
        this.status = status;
        this.type = type;
        this.payload = payload;
        this.lastUpdate = lastUpdate;
        this.readExpiration = readExpiration;
        this.performanceLimitTime = performanceLimitTime;
        this.allReadVersion = allReadVersion;
        this.allReadProcessingVersion = allReadProcessingVersion;
        this.message = message;
        this.compensation = compensation;
        this.lockedUntil = lockedUntil;
        this.readLockPeriod = readLockPeriod;
        this.readExpirationPeriod = readExpirationPeriod;


    }




    public static class OutboxModelJpaEntityBuilder {

        private UUID outboxUUID;

        private UUID processUUID;

        private OutboxStatus status;

        private String type;

        private String payload;

        private Instant lastUpdate;

        private Instant readExpiration;

        private Long performanceLimitTime;

        private Long allReadVersion;

        private Long allReadProcessingVersion;

        private String message;

        private boolean compensation;

        private Instant lockedUntil;


        // поля используются при сохранении waiting for signal ивента
        private Long readLockPeriod;

        // поля используются при сохранении waiting for signal ивента
        private Long readExpirationPeriod;




        OutboxModelJpaEntityBuilder() {
        }


        public OutboxModelJpaEntityBuilder outboxUUID(final UUID outboxUUID) {
            this.outboxUUID = outboxUUID;
            return this;
        }


        public OutboxModelJpaEntityBuilder processUUID(final UUID processUUID) {
            this.processUUID = processUUID;
            return this;
        }


        public OutboxModelJpaEntityBuilder status(final OutboxStatus status) {
            this.status = status;
            return this;
        }


        public OutboxModelJpaEntityBuilder type(final String type) {
            this.type = type;
            return this;
        }


        public OutboxModelJpaEntityBuilder payload(final String payload) {
            this.payload = payload;
            return this;
        }


        public OutboxModelJpaEntityBuilder lastUpdate(final Instant lastUpdate) {
            this.lastUpdate = lastUpdate;
            return this;
        }


        public OutboxModelJpaEntityBuilder readExpiration(final Instant readExpiration) {
            this.readExpiration = readExpiration;
            return this;
        }


        public OutboxModelJpaEntityBuilder performanceLimitTime(final Long performanceLimitTime) {
            this.performanceLimitTime = performanceLimitTime;
            return this;
        }


        public OutboxModelJpaEntityBuilder allReadVersion(final Long allReadVersion) {
            this.allReadVersion = allReadVersion;
            return this;
        }


        public OutboxModelJpaEntityBuilder allReadProcessingVersion(final Long allReadProcessingVersion) {
            this.allReadProcessingVersion = allReadProcessingVersion;
            return this;
        }


        public OutboxModelJpaEntityBuilder message(final String message) {
            this.message = message;
            return this;
        }


        public OutboxModelJpaEntityBuilder compensation(final boolean compensation) {
            this.compensation = compensation;
            return this;
        }


        public OutboxModelJpaEntityBuilder lockedUntil(final Instant lockedUntil) {
            this.lockedUntil = lockedUntil;
            return this;
        }


        public OutboxModelJpaEntity build() {
            return new OutboxModelJpaEntity(this.outboxUUID,
                    this.processUUID,
                    this.status,
                    this.type,
                    this.payload,
                    this.lastUpdate,
                    this.readExpiration,
                    this.performanceLimitTime,
                    this.allReadVersion,
                    this.allReadProcessingVersion,
                    this.message,
                    this.compensation,
                    this.lockedUntil,
                    this.readLockPeriod,
                    this.readExpirationPeriod);
        }


        public OutboxModelJpaEntityBuilder readExpirationPeriod(final Long readExpirationPeriod){

            this.readExpirationPeriod = readExpirationPeriod;


            return this;
        }

        public OutboxModelJpaEntityBuilder readLockPeriod(final Long readLockPeriod){
            this.readLockPeriod = readLockPeriod;

            return this;
        }


    }


}
