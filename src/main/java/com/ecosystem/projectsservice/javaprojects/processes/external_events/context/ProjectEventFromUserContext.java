package com.ecosystem.projectsservice.javaprojects.processes.external_events.context;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
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
public class ProjectEventFromUserContext implements ExternalEventContext {
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


    public static ProjectEventFromUserContext from(SecurityContext securityContext,
                                                   RequestContext requestContext,
                                                   Long projectId,
                                                   List<UUID> participants){

        return ProjectEventFromUserContext.builder()

                .correlationId(requestContext.getCorrelationId())
                .participants(participants)
                .projectId(projectId)
                .renderId(requestContext.getRenderId())
                .timestamp(Instant.now())
                .username(securityContext.getUsername())
                .userUUID(securityContext.getUuid())
                .build();

    }



}
