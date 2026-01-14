package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.prepared_chains.project_creation_from_template.event_structure;

import com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template.ProjectCreationStatus;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreationFromTemplateExternalData implements ExternalEventData {
    private Long projectId;
    private String name;
    private ProjectCreationStatus status;
    private String projectLanguage = "java";
}
