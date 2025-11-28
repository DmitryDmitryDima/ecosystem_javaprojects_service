package com.ecosystem.projectsservice.javaprojects.message_queue;

import com.ecosystem.projectsservice.javaprojects.message_queue.events_for_queue.ProjectRemovalQueueEvent;
import com.ecosystem.projectsservice.javaprojects.processes.events.entitiesflow.ProjectRemovalResultEvent;
import org.springframework.stereotype.Component;

@Component
public class EventConverter {


    public ProjectRemovalQueueEvent map(ProjectRemovalResultEvent projectRemovalResultEvent){
        return ProjectRemovalQueueEvent.builder().
                projectId(projectRemovalResultEvent.getProjectId())
                .status(projectRemovalResultEvent.getStatus()

                        )
                .message(projectRemovalResultEvent.getMessage())
                .context(projectRemovalResultEvent.getContext())
                .build();
    }



}
