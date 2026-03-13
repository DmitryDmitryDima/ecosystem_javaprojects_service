package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.directory_add;


import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectoryAddExternalData implements ExternalEventData {

    private long id;
    private String name;
    private long parentId;

}
