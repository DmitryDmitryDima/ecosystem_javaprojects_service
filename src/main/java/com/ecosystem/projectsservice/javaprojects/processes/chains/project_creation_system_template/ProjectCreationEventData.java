package com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template;

import com.ecosystem.projectsservice.javaprojects.processes.queue.EventData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectCreationEventData implements EventData {
    private Long projectId;
    private String name;
    private ProjectCreationStatus status;

    @Builder.Default
    private String projectLanguage = "java";

}
