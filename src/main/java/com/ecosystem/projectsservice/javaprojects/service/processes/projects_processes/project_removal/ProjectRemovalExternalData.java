package com.ecosystem.projectsservice.javaprojects.service.processes.projects_processes.project_removal;

import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRemovalExternalData implements ExternalEventData {


    private UUID projectId;

}
