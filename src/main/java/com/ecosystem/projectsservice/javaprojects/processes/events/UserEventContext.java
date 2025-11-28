package com.ecosystem.projectsservice.javaprojects.processes.events;

import lombok.*;

import java.time.Instant;
import java.util.UUID;


/*
данный контекст разделяют как внутренние, так и внешние события, связанные с действиями конкретного юзера
его смысл - сообщить notification сервису адресат и сообщить ui, от кого пришел ивент
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEventContext {
    private Instant timestamp;
    private String username;
    private UUID userUUID;
}
