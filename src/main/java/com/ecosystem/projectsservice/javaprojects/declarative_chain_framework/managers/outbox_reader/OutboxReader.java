package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.outbox_reader;



// чтение репозитория
public interface OutboxReader {




    void readWaitingEvents();

    void readExpiredWaitingEvents();

    void readExpiredProcessingEvents();

    void readEverlastingProcessingEvents();

    void readMissedExpiredProcessingEvents();

    void readManagerCrashedEvents();

    void readExpiredWaitingForSignalEvents();







}
