package com.ecosystem.projectsservice.javaprojects.processes.external_events.context;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
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
public class ProjectEventFromUserContext extends ExternalEventContext {

    private String username;
    private UUID userUUID;

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
