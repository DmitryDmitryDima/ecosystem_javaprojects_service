package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events;




import java.util.UUID;


public abstract class ChainEvent {

    // каждый процесс обязан иметь свой uuid
    private UUID processId;


    // id outbox ивента, из которого было прочитан ивент цепочки
    // используется для изменения статуса outbox ивента
    private UUID outboxId;

    private String message;

    private UUID parentProcess; // поле используется в сценариях,
    // когда цепочка была вызвана из какой либо родительской цепочки.
    // Предполагается использование триггера



    private ChainEventProcessingInfo processingInfo
            = new ChainEventProcessingInfo();




    public UUID getProcessId() {
        return this.processId;
    }


    public UUID getOutboxId() {
        return this.outboxId;
    }


    public String getMessage() {
        return this.message;
    }


    public UUID getParentProcess() {
        return this.parentProcess;
    }


    public ChainEventProcessingInfo getProcessingInfo() {
        return this.processingInfo;
    }


    public void setProcessId(final UUID processId) {
        this.processId = processId;
    }


    public void setOutboxId(final UUID outboxId) {
        this.outboxId = outboxId;
    }


    public void setMessage(final String message) {
        this.message = message;
    }


    public void setParentProcess(final UUID parentProcess) {
        this.parentProcess = parentProcess;
    }


    public void setProcessingInfo(final ChainEventProcessingInfo processingInfo) {
        this.processingInfo = processingInfo;
    }




}
