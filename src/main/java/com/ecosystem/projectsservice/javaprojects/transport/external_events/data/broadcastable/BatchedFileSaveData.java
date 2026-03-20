package com.ecosystem.projectsservice.javaprojects.transport.external_events.data.broadcastable;

import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



/*
специальный ивент, предназначенный для массового сохранения файлов
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchedFileSaveData implements ExternalEventData {

}
