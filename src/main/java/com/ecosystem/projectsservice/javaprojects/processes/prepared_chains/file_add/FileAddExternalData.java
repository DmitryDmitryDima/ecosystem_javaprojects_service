package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_add;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileAddExternalData implements ExternalEventData {

    private Long id;
    private String filename;
    private String extension;
    private long parentId;


}
