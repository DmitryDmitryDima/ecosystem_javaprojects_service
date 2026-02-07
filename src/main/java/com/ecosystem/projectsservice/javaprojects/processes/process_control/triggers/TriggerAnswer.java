package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

// универсальный ответ от триггера
@Data
@Builder
public class TriggerAnswer {

    // принято или нет решение
    private boolean decision;

    private String content;

    private UUID user;
    private UUID correlationId;
    private UUID renderId;



}
