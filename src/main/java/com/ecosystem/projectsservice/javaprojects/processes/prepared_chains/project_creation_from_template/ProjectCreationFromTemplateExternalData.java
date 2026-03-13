package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.project_creation_from_template;


import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreationFromTemplateExternalData implements ExternalEventData {
    private UUID projectId;
    private String name;
    private String projectLanguage = "java";
}
