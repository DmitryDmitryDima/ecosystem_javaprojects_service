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


    public ChainStep(final String name,
                     final String next,
                     final Method method,
                     final Long retry,
                     final ChainTimeUnit timeLimitUnit,
                     final Long timeLimit,
                     final ChainTimeUnit waitingForSignalUnit,
                     final Long waitingForSignal,
                     final boolean everlasting,
                     final List<StepExtension> extensions) {
        this.name = name;
        this.next = next;
        this.method = method;
        this.retry = retry;
        this.timeLimitUnit = timeLimitUnit;
        this.timeLimit = timeLimit;
        this.waitingForSignalUnit = waitingForSignalUnit;
        this.waitingForSignal = waitingForSignal;
        this.everlasting = everlasting;
        this.extensions = extensions;
    }


    public ChainStep() {
    }


}
