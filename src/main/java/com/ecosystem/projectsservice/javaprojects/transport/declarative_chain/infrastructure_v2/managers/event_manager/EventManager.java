package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModel;

// занимается чтением и интерпретацией состояний outbox модели
public interface EventManager {


    // просроченный ивент, помеченный как waiting_for_signal (означает, что он не дождался сигнала
    void workWithExpiredWaitingForSignalEvent(OutboxModel model);


    // ивент, зависший в processing
    // todo размышления об everlasting
    void workWithExpiredProcessingEvent(OutboxModel model);


    // обработка ивента, не дождавшегося прочтения
    void workWithExpiredWaitingEvent(OutboxModel model);


    // обыкновенный ивент - все хорошо
    void workWithWaitingEvent(OutboxModel model);



}
