package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.prepared_chains.project_removal.event_structure;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRemovalExternalData implements ExternalEventData {

    private Long projectId;
    private String projectName;
    private String projectLanguage = "java";

}
