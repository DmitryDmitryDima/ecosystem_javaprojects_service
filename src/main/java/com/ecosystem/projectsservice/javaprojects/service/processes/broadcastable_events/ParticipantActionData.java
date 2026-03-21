package com.ecosystem.projectsservice.javaprojects.service.processes.broadcastable_events;

import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ParticipantActionData implements ExternalEventData {
    private UUID uuid;
}
