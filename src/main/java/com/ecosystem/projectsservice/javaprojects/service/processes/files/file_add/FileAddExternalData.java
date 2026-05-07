package com.ecosystem.projectsservice.javaprojects.service.processes.files.file_add;

import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileAddExternalData implements ExternalEventData {

    private UUID id;
    private String filename;
    private String extension;
    private UUID parentId;
    private String constructedPath;


}
