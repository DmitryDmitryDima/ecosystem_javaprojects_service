package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_removal;

import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectoryRemovalExternalData implements ExternalEventData {

    private String path;
    private UUID id;
    private String name;


}
