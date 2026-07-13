package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.outbox_reader;



// чтение репозитория
public interface OutboxReader {




    void readWaitingEvents();

    void readExpiredWaitingEvents();

    void readExpiredProcessingEvents();

    void readEverlastingProcessingEvents();

    void readMissedExpiredProcessingEvents();

    void readManagerCrashedEvents();

    void readWaitingForSignalEvents();







}
