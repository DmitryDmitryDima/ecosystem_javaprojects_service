package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public abstract class Trigger {



    private String triggerMessage;
    private TriggerStrategy strategy;
    // триггер удаляется автоматически при просрочке
    private Instant expiredAt;

    private UUID parentProcess;

}
