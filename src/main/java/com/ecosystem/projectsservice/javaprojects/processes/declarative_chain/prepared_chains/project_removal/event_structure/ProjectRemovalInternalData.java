package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.prepared_chains.project_removal.event_structure;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.InternalEventData;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ProjectRemovalInternalData extends InternalEventData {

    private String projectPath;
    private String projectsPath;

}
