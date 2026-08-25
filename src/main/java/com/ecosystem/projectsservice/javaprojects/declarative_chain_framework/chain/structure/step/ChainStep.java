package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.structure.step;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.ChainTimeUnit;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class ChainStep  {


    // main info

    private String name; // указывается для каждого шага пользователем

    private String next; // указывается для каждого шага, кроме ending

    private Method method; // сохраняется в runtime для reflection запуска шагов


    // control info

    private Long retry;



    private ChainTimeUnit timeLimitUnit;
    private Long timeLimit;



    private ChainTimeUnit waitingForSignalUnit;
    private Long waitingForSignal;


    private ChainTimeUnit readExpirationUnit;
    private Long readExpiration;

    private ChainTimeUnit readLockUnit;
    private Long readLock;






    private boolean everlasting;






    // additionals


    // extension заносится при чтении шагов, обрабатывается в хуках
    private List<StepExtension> extensions = new ArrayList<>();



    public String getName() {
        return this.name;
    }


    public String getNext() {
        return this.next;
    }


    public Method getMethod() {
        return this.method;
    }


    public Long getRetry() {
        return this.retry;
    }


    public ChainTimeUnit getTimeLimitUnit() {
        return this.timeLimitUnit;
    }


    public Long getTimeLimit() {
        return this.timeLimit;
    }


    public ChainTimeUnit getWaitingForSignalUnit() {
        return this.waitingForSignalUnit;
    }


    public Long getWaitingForSignal() {
        return this.waitingForSignal;
    }


    public boolean isEverlasting() {
        return this.everlasting;
    }


    public List<StepExtension>getExtensions() {
        return this.extensions;
    }


    public void setName(final String name) {
        this.name = name;
    }


    public void setNext(final String next) {
        this.next = next;
    }


    public void setMethod(final Method method) {
        this.method = method;
    }


    public void setRetry(final Long retry) {
        this.retry = retry;
    }


    public void setTimeLimitUnit(final ChainTimeUnit timeLimitUnit) {
        this.timeLimitUnit = timeLimitUnit;
    }


    public void setTimeLimit(final Long timeLimit) {
        this.timeLimit = timeLimit;
    }


    public void setWaitingForSignalUnit(final ChainTimeUnit waitingForSignalUnit) {
        this.waitingForSignalUnit = waitingForSignalUnit;
    }


    public void setWaitingForSignal(final Long waitingForSignal) {
        this.waitingForSignal = waitingForSignal;
    }


    public void setEverlasting(final boolean everlasting) {
        this.everlasting = everlasting;
    }


    public void setExtensions(final List<StepExtension> extensions) {
        this.extensions = extensions;
    }

    public Long getReadExpiration() {
        return readExpiration;
    }

    public void setReadExpiration(Long readExpiration) {
        this.readExpiration = readExpiration;
    }

    public ChainTimeUnit getReadExpirationUnit() {
        return readExpirationUnit;
    }

    public void setReadExpirationUnit(ChainTimeUnit readExpirationUnit) {
        this.readExpirationUnit = readExpirationUnit;
    }

    public ChainTimeUnit getReadLockUnit() {
        return readLockUnit;
    }

    public void setReadLockUnit(ChainTimeUnit readLockUnit) {
        this.readLockUnit = readLockUnit;
    }

    public Long getReadLock() {
        return readLock;
    }

    public void setReadLock(Long readLock) {
        this.readLock = readLock;
    }

    public ChainStep(String next,
                     Method method,
                     String name,
                     Long retry,
                     ChainTimeUnit timeLimitUnit,
                     Long timeLimit,
                     ChainTimeUnit waitingForSignalUnit,
                     Long waitingForSignal,
                     ChainTimeUnit readExpirationUnit,
                     Long readExpiration,
                     ChainTimeUnit readLockUnit,
                     Long readLock,
                     boolean everlasting,
                     List<StepExtension> extensions) {
        this.next = next;
        this.method = method;
        this.name = name;
        this.retry = retry;
        this.timeLimitUnit = timeLimitUnit;
        this.timeLimit = timeLimit;
        this.waitingForSignalUnit = waitingForSignalUnit;
        this.waitingForSignal = waitingForSignal;
        this.readExpirationUnit = readExpirationUnit;
        this.readExpiration = readExpiration;
        this.readLockUnit = readLockUnit;
        this.readLock = readLock;
        this.everlasting = everlasting;
        this.extensions = extensions;
    }

    public ChainStep() {
    }


}
