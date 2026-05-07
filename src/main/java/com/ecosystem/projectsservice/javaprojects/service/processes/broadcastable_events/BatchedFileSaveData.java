package com.ecosystem.projectsservice.javaprojects.service.processes.broadcastable_events;

import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;



/*
специальный ивент, предназначенный для массового сохранения файлов
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchedFileSaveData implements ExternalEventData {

    private Map<UUID, String> contentMap;
}
