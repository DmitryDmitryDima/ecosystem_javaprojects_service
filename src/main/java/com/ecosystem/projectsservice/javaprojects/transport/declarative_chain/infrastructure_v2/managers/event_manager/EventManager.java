package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModel;

// занимается чтением и интерпретацией состояний outbox модели
public interface EventManager {





    ManagementResult workWithWaitingEvent(OutboxModel model);

    ManagementResult workWithExpiredWaitingEvent(OutboxModel model);



    ManagementResult workWithEverlastingProcessingEvent(OutboxModel model);


    ManagementResult workWithExpiredProcessingEvent(OutboxModel model);


    // отправляется в dead letter сервис
    ManagementResult workWithMissedExpiredProcessingEvent(OutboxModel model);


    // отправляется в dead letter сервис
    ManagementResult workWithManagerCrashEvent(OutboxModel model);


    ManagementResult workWithExpiredWaitingForSignalEvent(OutboxModel model);

















}
