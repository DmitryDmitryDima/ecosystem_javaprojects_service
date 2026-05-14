package com.ecosystem.projectsservice.javaprojects.service.processes.projects_processes.project_removal;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.Compensator;
import org.springframework.stereotype.Component;


@Component
public class ProjectRemovalCompensator implements Compensator<ProjectRemovalEvent> {
    @Override
    public void compensation(ProjectRemovalEvent event) {

    }
}
