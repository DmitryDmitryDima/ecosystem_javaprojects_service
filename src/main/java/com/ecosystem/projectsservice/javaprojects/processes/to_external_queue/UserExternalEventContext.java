package com.ecosystem.projectsservice.javaprojects.processes.to_external_queue;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventContext;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


/*
данный контекст разделяют как внутренние, так и внешние события, связанные с действиями конкретного юзера
его смысл - сообщить notification сервису адресат и далее, через websocket рассылку сообщить ui, от кого пришел ивент
таким образом, каждый ивент, который имеет источник действия в виде пользователя, имеет такой контекст



 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserExternalEventContext implements ExternalEventContext {
    private Instant timestamp;
    private String username;
    private UUID userUUID;
    private UUID correlationId;
}
