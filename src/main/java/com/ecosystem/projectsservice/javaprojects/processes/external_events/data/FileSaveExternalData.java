package com.ecosystem.projectsservice.javaprojects.processes.external_events.data;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileSaveExternalData implements ExternalEventData {

    private Long fileId;
    private String name;
    private String extension;
    private String content;
    private String path;
    private UUID fileOwner;


}


