package com.ecosystem.projectsservice.javaprojects.service.processes.files.filesave;

import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileSaveExternalData implements ExternalEventData {

    private UUID fileId;
    private String name;
    private String extension;
    private String content;
    private String path;
    private UUID fileOwner;


}


