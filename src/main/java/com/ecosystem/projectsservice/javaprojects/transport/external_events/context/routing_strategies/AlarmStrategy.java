package com.ecosystem.projectsservice.javaprojects.transport.external_events.context.routing_strategies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// если требуется персональная рассылка + какое то действие в notification слое (например - закрыть сессию)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmStrategy {
    private List<UUID> alarmList = new ArrayList<>();
    private AlarmAction action;

}
