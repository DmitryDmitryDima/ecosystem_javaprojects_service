package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

import lombok.Data;

import java.util.UUID;

@Data
public abstract class UserTriggerAnswer {
    private UUID correlationId;
    private UUID from;
    private UUID render;
}
