package com.ecosystem.projectsservice.javaprojects.processes.prepared.project_removal.event_structure;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRemovalExternalData implements ExternalEventData {


    private Long projectId;

}
