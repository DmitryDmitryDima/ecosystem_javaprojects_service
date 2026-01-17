package com.ecosystem.projectsservice.javaprojects.processes.prepared.project_removal.event_structure;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.InternalEventData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRemovalInternalData extends InternalEventData {

    private String projectPath;
}
