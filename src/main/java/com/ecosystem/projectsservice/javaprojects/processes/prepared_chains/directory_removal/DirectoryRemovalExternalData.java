package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.directory_removal;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectoryRemovalExternalData implements ExternalEventData {

    private String path;
    private Long id;
    private String name;


}
