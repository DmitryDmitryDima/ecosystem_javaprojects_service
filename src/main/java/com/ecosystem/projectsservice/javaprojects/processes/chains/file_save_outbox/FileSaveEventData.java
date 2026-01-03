package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox;

import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.ExternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSaveEventData implements ExternalEventData {
    private Long fileId;
    private String name;
    private String content;
    private String path;
    private EventStatus status;

}
