package com.ecosystem.projectsservice.javaprojects.processes.project_removal.event_structure;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRemovalExternalData implements ExternalEventData {


    private Long projectId;

}
