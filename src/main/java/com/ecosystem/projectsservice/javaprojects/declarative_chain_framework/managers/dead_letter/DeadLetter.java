package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.dead_letter;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output.ChainOutput;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output.OutputMetadata;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.avatar.ProcessAvatarStatus;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModel;

import java.util.UUID;


public class DeadLetter {





    // последние данные у аватара перед смертью
    private ChainOutput previousOutput;

    private OutputMetadata<?> previousOutputMetadata;

    private ProcessAvatarStatus lastStatus;



    private String message;

    private OutboxModel model;

    // проставляется в случае, если отсутствует сформированная модель
    private UUID outboxId;

    private UUID processUUID; // проставляется в случае, если отсутствует сформированная модель

    public DeadLetter(String message, OutboxModel model) {
        this.message = message;
        this.model = model;
    }

    public DeadLetter(String message, UUID outboxId, UUID processUUID){
        this.message = message;
        this.outboxId = outboxId;
        this.processUUID = processUUID;
    }




    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OutboxModel getModel() {
        return model;
    }

    public void setModel(OutboxModel model) {
        this.model = model;
    }

    public UUID getOutboxId() {
        return outboxId;
    }

    public void setOutboxId(UUID outboxId) {
        this.outboxId = outboxId;
    }

    public UUID getProcessUUID() {
        return processUUID;
    }

    public void setProcessUUID(UUID processUUID) {
        this.processUUID = processUUID;
    }
}
