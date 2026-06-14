package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events;


import lombok.*;

import java.util.UUID;

@Getter
@Setter
public abstract class ChainEvent {

    // каждый процесс обязан иметь свой uuid
    private UUID processId;


    // id outbox ивента, из которого было прочитан ивент цепочки
    // используется для изменения статуса outbox ивента
    private UUID outboxId;

    private String message;



    private ChainEventProcessingInfo processingInfo
            = new ChainEventProcessingInfo();
















}
