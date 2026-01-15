package com.ecosystem.projectsservice.javaprojects.processes.project_creation_from_template.event_structure;


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
    private String projectLanguage = "java";
}
