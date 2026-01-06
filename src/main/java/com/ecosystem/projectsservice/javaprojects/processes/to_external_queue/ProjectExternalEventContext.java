package com.ecosystem.projectsservice.javaprojects.processes.to_external_queue;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventContext;
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
public class ProjectExternalEventContext implements ExternalEventContext {
    private Instant timestamp;
    private String username;
    private UUID userUUID;
    private UUID correlationId;
    private UUID renderId;

    // todo participant role - роль участника - к примеру author, project admin (not always author only)

    private Long projectId;

    // все остальные, кроме автора ивента. Автор при этом тоже получает событие
    // при этом важно отметить, что данное поле не всегда используется
    // - к примеру ивент сохранения файла достаточно отправить только через project_id. т.е. по адресу комнаты
    private List<UUID> participants;



}
