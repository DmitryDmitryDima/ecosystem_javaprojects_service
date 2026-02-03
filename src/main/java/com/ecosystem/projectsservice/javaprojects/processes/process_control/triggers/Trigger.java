package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
@SuperBuilder
public abstract class Trigger {




    private String triggerMessage;
    private TriggerStrategy strategy;
    // триггер удаляется автоматически при просрочке
    private Instant expiredAt = Instant.now().plusSeconds(60*60); // дефолт - час после expire

    private UUID parentProcess;

    // опциональные поля для большего контроля
    private UUID authorRenderUUID;
    private UUID authorUUID;

    private AtomicBoolean activityGained = new AtomicBoolean(false);


    public abstract TriggerType getTriggerType();


}
