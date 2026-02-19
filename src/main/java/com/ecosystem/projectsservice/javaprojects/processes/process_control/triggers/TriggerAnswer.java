package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

// универсальный ответ для триггера
@Data
@Builder
public class TriggerAnswer {

    // принято или нет решение
    private boolean decision;

    private String content;

    private UUID user;
    private UUID correlationId;
    private UUID renderId;

    // если общение происходит с какой-либо системой, то она может использовать это поле вместо uuid
    private String pseudonym;

    @Override
    public String toString() {
        return "TriggerAnswer{" +
                "decision=" + decision +
                ", content='" + content + '\'' +
                ", user=" + user +
                '}';
    }
}
