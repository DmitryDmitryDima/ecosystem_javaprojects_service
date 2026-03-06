package com.ecosystem.projectsservice.javaprojects.processes.external_events.data;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
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
