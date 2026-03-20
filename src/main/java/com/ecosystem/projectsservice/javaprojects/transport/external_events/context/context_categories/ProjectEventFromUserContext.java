package com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.routing_strategies.AlarmStrategy;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.routing_strategies.NotificationStrategy;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
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

    private UUID projectId;






    public static ProjectEventFromUserContext from (SecurityContext securityContext,
                                                    RequestContext requestContext,
                                                    Project project, NotificationStrategy notificationStrategy,
                                                    AlarmStrategy alarmStrategy){

        return ProjectEventFromUserContext.builder()


                .correlationId(requestContext.getCorrelationId())
                .notificationStrategy(notificationStrategy)
                .alarmStrategy(alarmStrategy)

                .projectId(project.getId())

                .renderId(requestContext.getRenderId())
                .timestamp(Instant.now())
                .username(securityContext.getUsername())
                .userUUID(securityContext.getUuid())
                .build();


    }



}
