package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger;


import java.util.UUID;

// может быть наследован, а также может применяться как самостоятельный класс
public class TriggerFeed {


    private String data;


    // предполагается, что origin является уникальным ключом
    private String origin;

    private UUID processId;


    public TriggerFeed(UUID processId, String data, String origin) {
        this.data = data;
        this.origin = origin;
        this.processId = processId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public UUID getProcessId() {
        return processId;
    }

    public void setProcessId(UUID processId) {
        this.processId = processId;
    }
}
