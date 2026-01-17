package com.ecosystem.projectsservice.javaprojects.processes.prepared.filesave.event_structure;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileSaveExternalData implements ExternalEventData {

    private Long fileId;
    private String name;
    private String content;
    private String path;


}


