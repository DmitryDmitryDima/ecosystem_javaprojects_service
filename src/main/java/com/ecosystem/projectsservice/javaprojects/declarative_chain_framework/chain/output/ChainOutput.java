package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxStatus;


import java.time.Instant;



public class ChainOutput {




    private ChainEvent event;

    private OutboxStatus status;
    // последняя смена статуса
    private Instant last_update;

    // время, до которого ивент должен быть прочитан (комбинация с waiting for или waiting)
    private Instant readExpiration;

    // время, за которое ивент должен получить состояние processed - милисекунды
    private Long performanceExpirationPeriod;


    // для waiting ивента lock until высчитывается сразу
    private Instant lockUntil;

    // проставляется, когда публикуется waiting for signal
    private Long readExpirationPeriod;

    // проставляется, когда публикуется waiting for signal
    private Long readLockPeriod;



    public ChainOutput(){}

    public ChainOutput(final ChainEvent event,
                       final OutboxStatus status,
                       final Instant last_update,
                       final Instant readExpiration,
                       final Long performanceExpirationPeriod,
                       final Instant lockUntil,
                       final Long readLockPeriod,
                       final Long readExpirationPeriod) {

        this.event = event;
        this.status = status;
        this.last_update = last_update;
        this.readExpiration = readExpiration;
        this.performanceExpirationPeriod = performanceExpirationPeriod;
        this.readLockPeriod = readLockPeriod;
        this.readExpirationPeriod = readExpirationPeriod;
        this.lockUntil = lockUntil;
    }















    public static ChainOutputBuilder builder() {
        return new ChainOutputBuilder();
    }






    public ChainEvent getEvent() {
        return this.event;
    }


    public OutboxStatus getStatus() {
        return this.status;
    }


    public Instant getLast_update() {
        return this.last_update;
    }


    public Instant getReadExpiration() {
        return this.readExpiration;
    }


    public Long getPerformanceExpirationPeriod() {
        return this.performanceExpirationPeriod;
    }


    public void setEvent(final ChainEvent event) {
        this.event = event;
    }


    public void setStatus(final OutboxStatus status) {
        this.status = status;
    }


    public void setLast_update(final Instant last_update) {
        this.last_update = last_update;
    }


    public void setReadExpiration(final Instant readExpiration) {
        this.readExpiration = readExpiration;
    }


    public void setPerformanceExpirationPeriod(final Long performanceExpirationPeriod) {
        this.performanceExpirationPeriod = performanceExpirationPeriod;
    }


    public Long getReadExpirationPeriod() {
        return readExpirationPeriod;
    }

    public void setReadExpirationPeriod(final Long readExpirationPeriod) {
        this.readExpirationPeriod = readExpirationPeriod;
    }

    public Long getReadLockPeriod() {
        return readLockPeriod;
    }

    public void setReadLockPeriod(final Long readLockPeriod) {
        this.readLockPeriod = readLockPeriod;
    }

    public Instant getLockUntil() {
        return lockUntil;
    }

    public void setLockUntil(Instant lockUntil) {
        this.lockUntil = lockUntil;
    }

    public static class ChainOutputBuilder {

        private ChainEvent event;

        private OutboxStatus status;

        private Instant last_update;

        private Instant readExpiration;

        private Long performanceExpirationPeriod;

        private Instant lockUntil;

        // проставляется, когда публикуется waiting for signal
        private Long readExpirationPeriod;

        // проставляется, когда публикуется waiting for signal
        private Long readLockPeriod;







        ChainOutputBuilder() {
        }


        public ChainOutputBuilder event(final ChainEvent event) {
            this.event = event;
            return this;
        }

        public ChainOutputBuilder lockUntil(final Instant lockUntil){
            this.lockUntil = lockUntil;
            return this;
        }


        public ChainOutputBuilder status(final OutboxStatus status) {
            this.status = status;
            return this;
        }


        public ChainOutputBuilder last_update(final Instant last_update) {
            this.last_update = last_update;
            return this;
        }


        public ChainOutputBuilder readExpiration(final Instant readExpiration) {
            this.readExpiration = readExpiration;
            return this;
        }


        public ChainOutputBuilder performanceExpirationPeriod(final Long performanceExpirationPeriod) {
            this.performanceExpirationPeriod = performanceExpirationPeriod;
            return this;
        }

        public ChainOutputBuilder readExpirationPeriod(final Long readExpirationPeriod){

            this.readExpirationPeriod = readExpirationPeriod;


            return this;
        }

        public ChainOutputBuilder readLockPeriod(final Long readLockPeriod){
            this.readLockPeriod = readLockPeriod;

            return this;
        }


        public ChainOutput build() {
            return new ChainOutput(this.event,
                    this.status,
                    this.last_update,
                    this.readExpiration,
                    this.performanceExpirationPeriod,
                    this.lockUntil,
                    this.readLockPeriod,
                    this.readExpirationPeriod
                    );
        }
    }




}
