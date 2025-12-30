package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save;

import com.ecosystem.projectsservice.javaprojects.processes.external_queue.EventData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSaveEventData implements EventData {
    private Long fileId;
    private String name;
    private String content;
    private String path;
    private FileSaveStatus status;
}
