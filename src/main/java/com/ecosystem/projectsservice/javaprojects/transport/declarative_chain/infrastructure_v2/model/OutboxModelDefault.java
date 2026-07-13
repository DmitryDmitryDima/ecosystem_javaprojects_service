package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.id.uuid.UuidVersion7Strategy;

import java.time.Instant;
import java.util.UUID;

public class OutboxModelDefault implements OutboxModel{



    private UUID outboxUUID;



    private UUID processUUID;


    private OutboxStatus status;

    private String type;


    private String payload;

    private Instant lastUpdate;

    private Instant readExpiration;

    private Long performanceLimitTime;

    private Long allReadVersion = 0L;

    private Long allReadProcessingVersion = 0L;

    private String message;






    public void setOutboxUUID(UUID outboxUUID) {
        this.outboxUUID = outboxUUID;
    }

    public void setProcessUUID(UUID processUUID) {
        this.processUUID = processUUID;
    }

    public void setStatus(OutboxStatus status) {
        this.status = status;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setReadExpiration(Instant readExpiration) {
        this.readExpiration = readExpiration;
    }

    public void setPerformanceLimitTime(Long performanceLimitTime) {
        this.performanceLimitTime = performanceLimitTime;
    }

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
    public String getMessage(){

        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setAllReadVersion(Long allReadVersion) {
        this.allReadVersion = allReadVersion;
    }

    public void setAllReadProcessingVersion(Long allReadProcessingVersion) {
        this.allReadProcessingVersion = allReadProcessingVersion;
    }



}
