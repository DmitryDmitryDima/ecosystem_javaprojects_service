package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.step;

import java.time.Instant;

public class StepCountedTime {


    private Instant lastUpdate;


    private Instant currentReadExpiration;

    private Instant lockUntil;

    private Long readExpirationPeriod;


    private Long readLockPeriod;

    private Long duration;

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Instant getCurrentReadExpiration() {
        return currentReadExpiration;
    }

    public void setCurrentReadExpiration(Instant currentReadExpiration) {
        this.currentReadExpiration = currentReadExpiration;
    }

    public Instant getLockUntil() {
        return lockUntil;
    }

    public void setLockUntil(Instant lockUntil) {
        this.lockUntil = lockUntil;
    }

    public Long getReadExpirationPeriod() {
        return readExpirationPeriod;
    }

    public void setReadExpirationPeriod(Long readExpirationPeriod) {
        this.readExpirationPeriod = readExpirationPeriod;
    }



    public Long getReadLockPeriod() {
        return readLockPeriod;
    }

    public void setReadLockPeriod(Long readLockPeriod) {
        this.readLockPeriod = readLockPeriod;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
