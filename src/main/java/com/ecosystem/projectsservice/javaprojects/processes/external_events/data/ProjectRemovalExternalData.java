package com.ecosystem.projectsservice.javaprojects.processes.external_events.data;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
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
