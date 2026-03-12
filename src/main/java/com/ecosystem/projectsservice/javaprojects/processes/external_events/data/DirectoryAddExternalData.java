package com.ecosystem.projectsservice.javaprojects.processes.external_events.data;


import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
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
