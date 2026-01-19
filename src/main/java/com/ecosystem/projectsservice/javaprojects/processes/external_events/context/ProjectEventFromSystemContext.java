package com.ecosystem.projectsservice.javaprojects.processes.external_events.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectEventFromSystemContext implements ExternalEventContext{

    private Instant timestamp;

    private Long projectId;

    // название системного процесса (опционально)
    private String origin;

    // correlation id процесса - каждый процесс в системе должен иметь свой correlation id
    private UUID correlationId;

    // участники проекта - опционально для случаев, где требуется персональная рассылка участникам проекта
    private List<UUID> participants;
}
