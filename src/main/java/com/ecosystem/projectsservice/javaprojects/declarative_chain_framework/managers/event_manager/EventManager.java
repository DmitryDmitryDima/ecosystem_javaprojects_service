package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.event_manager;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModel;

// занимается чтением и интерпретацией состояний outbox модели
public interface EventManager {





    ManagerResult workWithWaitingEvent(OutboxModel model);

    ManagerResult workWithExpiredWaitingEvent(OutboxModel model);



    ManagerResult workWithEverlastingProcessingEvent(OutboxModel model);


    ManagerResult workWithExpiredProcessingEvent(OutboxModel model);


    // отправляется в dead letter сервис
    ManagerResult workWithMissedExpiredProcessingEvent(OutboxModel model);


    // отправляется в dead letter сервис
    ManagerResult workWithManagerCrashEvent(OutboxModel model);


    ManagerResult workWithExpiredWaitingForSignalEvent(OutboxModel model);

















}
