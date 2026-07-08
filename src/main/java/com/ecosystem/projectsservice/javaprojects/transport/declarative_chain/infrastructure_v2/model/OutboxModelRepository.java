package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model;


import java.util.List;
import java.util.UUID;

// контракт, позволяющий связать core функционал с конкретным способом доставки
public interface OutboxModelRepository {


    void save(OutboxModel model);


    // помечает outbox запись как processed, выкидывая ее из ряда ждущих обработки
    void markAsProcessed(UUID id);






    // читаем ивенты, где readVersion == 0, performancePeriod = null
    // атомарно readVersion ++;

    List<OutboxModel> readEverlastingProcessingEvents(Long batchSize);

    List<OutboxModel> readEverlastingProcessingEvents();


    // читаем processing ивенты, где readVersion!=0 - это означает попадание в dead letter
    // при чтении такие ивенты атомарно получают processed

    List<OutboxModel> readMissedExpiredProcessingEvents();

    List<OutboxModel> readMissedExpiredProcessingEvents(Long batchSize);


    // читаем expired processing ивенты, где readVersion = 0,
    // now > performancePeriod + lastUpdate
    // НЮАНС - ПРИ ЧТЕНИИ WAITING EVENT'А СЧЕТЧИК ЧТЕНИЯ НЕ ТРОГАЕТСЯ,
    // ТАК КАК ЗА ИДЕМПОТЕНТНОСТЬ ОТВЕЧАЕТ СМЕНА СТАТУСА
    // атомарно - readVersion ++

    List<OutboxModel> readExpiredProcessingEvents();

    List<OutboxModel> readExpiredProcessingEvents(Long batchSize);


    // читаем актуальные waiting events - самая высокая частота проверки
    // атомарно ставим статус processing
    List<OutboxModel> readActualWaitingEvents();
    List<OutboxModel> readActualWaitingEvents(Long batchSize);

    // читаем просроченные waiting event
    // атомарно processing

    List<OutboxModel> readExpiredWaitingEvents();
    List<OutboxModel> readExpiredWaitingEvents(Long batchSize);


    // читаем просроченные waiting for signal ивенты
    // атомарно processing

    List<OutboxModel> readExpiredWaitingForSignalEvents();
    List<OutboxModel> readExpiredWaitingForSignalEvents(Long batchSize);
















}
