package com.ecosystem.projectsservice.javaprojects.transport.external_events.context.routing_strategies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationStrategy {

    // персональное сообщение в приватный канал
    private List<UUID> privateChannel;

    //  персональное сообщение в публичный канал
    private List<UUID> publicChannel;


}
