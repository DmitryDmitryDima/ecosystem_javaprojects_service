package com.ecosystem.projectsservice.javaprojects.processes.external_events.context;


import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder

public abstract class ExternalEventContext {

    // correlation id процесса - каждый процесс в системе должен иметь свой correlation id
    private UUID correlationId;

    private Instant timestamp;





}
