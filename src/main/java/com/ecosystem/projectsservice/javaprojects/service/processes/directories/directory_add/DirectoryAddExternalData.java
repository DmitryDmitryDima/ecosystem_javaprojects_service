package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add;


import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectoryAddExternalData implements ExternalEventData {

    private UUID id;
    private String name;
    private UUID parentId;

}
