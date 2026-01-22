package com.ecosystem.projectsservice.javaprojects.processes.external_events.context;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;


/*
данный контекст разделяют как внутренние, так и внешние события, связанные с действиями конкретного юзера
его смысл - сообщить notification сервису адресат и далее, через websocket рассылку сообщить ui, от кого пришел ивент
таким образом, каждый ивент, который имеет источник действия в виде пользователя, имеет такой контекст



 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserPersonalEventContext extends ExternalEventContext {

    private String username;
    private UUID userUUID;

    private UUID renderId;
}
