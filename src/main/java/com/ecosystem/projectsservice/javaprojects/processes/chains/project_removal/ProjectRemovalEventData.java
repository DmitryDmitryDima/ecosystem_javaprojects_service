package com.ecosystem.projectsservice.javaprojects.processes.chains.project_removal;

import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectRemovalEventData implements ExternalEventData {

    private ProjectRemovalStatus status;
    private Long projectId;
    private String projectName;


    @Builder.Default
    private String projectLanguage = "java";


}
