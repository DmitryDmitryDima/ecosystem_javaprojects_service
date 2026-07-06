package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model;


import java.util.List;
import java.util.UUID;

// контракт, позволяющий связать core функционал с конкретным способом доставки
public interface OutboxModelRepository {


    void save(OutboxModel model);


    // помечает outbox запись как processed, выкидывая ее из ряда ждущих обработки
    void markAsProcessed(UUID id);


    List<? extends OutboxModel> getByStatus(OutboxStatus status);

    List<? extends OutboxModel> getByStatus(OutboxStatus status, Integer limit);






}
