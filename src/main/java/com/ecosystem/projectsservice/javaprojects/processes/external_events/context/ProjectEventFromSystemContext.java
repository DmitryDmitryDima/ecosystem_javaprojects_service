package com.ecosystem.projectsservice.javaprojects.processes.external_events.context;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ProjectEventFromSystemContext extends ExternalEventContext{



    private Long projectId;

    // название системного процесса (опционально)
    private String origin;



    // участники проекта - опционально для случаев, где требуется персональная рассылка участникам проекта
    private List<UUID> participants;
}
