package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model;


import java.util.List;
import java.util.UUID;

// контракт, позволяющий связать core функционал с конкретным способом доставки
public interface OutboxModelRepository {


    void create(OutboxModel model);


    void markPreviousAsProcessedAndCreateNewModel(UUID previous, OutboxModel model);


    // помечает outbox запись как processed,
    // при этом запись запись должна иметь compensation = false и статус processing
    void markAsProcessedForSuccessStep(UUID id);


    // помечает, как processed,
    // при этом запись должна иметь processing в качестве предыдущего статуса
    void markAsProcessedForCompensation(UUID id);


    void markAsCompensating(UUID id);




    // проставление статуса с учетом последней версии (race condition free)
    void changeStatusForGivenAllReadVersion(UUID uuid,
                                   OutboxStatus toStatus,
                                   Long forAllReadVersion);




    // проставление статуса с учетом последней версии, с сообщением
    void changeStatusAndMessageForGivenAllReadVersion(UUID uuid,
                                                      OutboxStatus toStatus, String message,
                                                      Long forAllReadVersion);









    // performance period == null
    // allProcessingRead ++

    List<OutboxModel> readEverlastingProcessingEvents(Long batchSize);

    List<OutboxModel> readEverlastingProcessingEvents();


    // читаем processing ивенты, где allProcessingRead!=0 && period ! =null
    // - это означает попадание в dead letter
    // при чтении такие ивенты атомарно получают dead_letter

    List<OutboxModel> readMissedExpiredProcessingEvents();

    List<OutboxModel> readMissedExpiredProcessingEvents(Long batchSize);


    // читаем expired processing ивенты, где allProcessingRead = 0,
    // now > performancePeriod + lastUpdate
    // атомарно - allProcessingRead++

    List<OutboxModel> readExpiredProcessingEvents();

    List<OutboxModel> readExpiredProcessingEvents(Long batchSize);


    // читаем актуальные waiting events - самая высокая частота проверки
    // атомарно ставим статус processing
    // в данном случае allProcessingRead не трогаем !
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





    List<OutboxModel> readManagerCrashEvents();
    List<OutboxModel> readManagerCrashEvents(Long batchSize);




















}
