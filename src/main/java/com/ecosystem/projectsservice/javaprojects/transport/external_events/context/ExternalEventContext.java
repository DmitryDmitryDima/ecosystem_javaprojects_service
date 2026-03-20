package com.ecosystem.projectsservice.javaprojects.transport.external_events.context;


import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.routing_strategies.AlarmStrategy;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.routing_strategies.NotificationStrategy;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class ExternalEventContext {

    // correlation id процесса - каждый процесс в системе должен иметь свой correlation id
    private UUID correlationId;

    private Instant timestamp;

    // alarm стратегия - стратегия для notification слоя
    private AlarmStrategy alarmStrategy;

    // инструкция, согласно которой notification service производит рассылку. Сделана максимально гибкой из за разности сценариев
    private NotificationStrategy notificationStrategy;

    // ПРИМЕЧАНИЕ - ALARM STRATEGY И NOTIFICATION STRATEGY ЯВЛЯЮТСЯ ДОПОЛНЕНИЕМ К ДЕФОЛТНОМУ ПОВЕДЕНИЮ КАЖДОЙ ИЗ КАТЕГОРИЙ.





}
