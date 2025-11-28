package com.ecosystem.projectsservice.javaprojects.message_queue.events_for_queue;

import com.ecosystem.projectsservice.javaprojects.processes.events.UserEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.events.entitiesflow.ProjectRemovalResultEvent;
import com.ecosystem.projectsservice.javaprojects.processes.events.status.ProjectRemovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProjectRemovalQueueEvent {

    private ProjectRemovalStatus status;

    private String message;

    private Long projectId;

    // задел под расширение
    @Builder.Default
    private String projectLanguage = "java";

    private UserEventContext context;

}
