package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.read_limits;


// интерфейс, который явно проставить в системе лимиты для каждого из запросов к хранилищу
// Данный сервис используется reader'ом
public interface ReadLimits {


    int waitingEventsLimit();

    int expiredWaitingEventsLimit();

    int expiredProcessingEventsLimit();

    int everlastingProcessingEventsLimit();

    int missingExpiredProcessingEventsLimit();

    int managerCrashedEventsLimit();

    int expiredWaitingForSignalEventsLimit();
}
