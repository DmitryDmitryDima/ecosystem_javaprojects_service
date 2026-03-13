package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_removal;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileRemovalExternalData implements ExternalEventData {
    private Long fileId;
    private String name;
    private String extension;
    private String path;
    private UUID fileOwner;
}
