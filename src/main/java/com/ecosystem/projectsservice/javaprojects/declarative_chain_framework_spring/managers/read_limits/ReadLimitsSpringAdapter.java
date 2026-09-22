package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.managers.read_limits;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.read_limits.ReadLimits;
import org.springframework.beans.factory.annotation.Value;

public class ReadLimitsSpringAdapter implements ReadLimits {

    @Value("${limiter.waiting.events:100}")
    private int waitingEventsLimit;


    @Value("${limiter.waiting.events.expired:100}")
    private int expiredWaitingEventsLimit;

    @Value("${limiter.processing.events.expired:100}")
    private int expiredProcessingEventsLimit;

    @Value("${limiter.processing.events.everlasting:100}")
    private int everlastingProcessingEventsLimit;

    @Value("${limiter.processing.events.missing:100}")
    private int missingExpiredProcessingEventsLimit;

    @Value("${limiter.crashed.events:100}")
    private int managerCrashedEventsLimit;

    @Value("${limiter.waiting.for.signal.events:100}")
    private int expiredWaitingForSignalEventsLimit;














    @Override
    public int waitingEventsLimit() {
        return waitingEventsLimit;
    }

    @Override
    public int expiredWaitingEventsLimit() {
        return expiredWaitingEventsLimit;
    }

    @Override
    public int expiredProcessingEventsLimit() {
        return expiredProcessingEventsLimit;
    }

    @Override
    public int everlastingProcessingEventsLimit() {
        return everlastingProcessingEventsLimit;
    }

    @Override
    public int missingExpiredProcessingEventsLimit() {
        return missingExpiredProcessingEventsLimit;
    }

    @Override
    public int managerCrashedEventsLimit() {
        return managerCrashedEventsLimit;
    }

    @Override
    public int expiredWaitingForSignalEventsLimit() {
        return expiredWaitingForSignalEventsLimit;
    }
}
