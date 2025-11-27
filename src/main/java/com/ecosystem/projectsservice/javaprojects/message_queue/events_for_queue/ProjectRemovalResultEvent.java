package com.ecosystem.projectsservice.javaprojects.message_queue.events_for_queue;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
@ToString
public class ProjectRemovalResultEvent extends ApplicationEvent {

    // имя ивента в системе распределенной очереди
    private final String event_type = "java_project_removal";

    private ProjectRemovalStatus status;
    private String message;
    private Long projectId;
    public ProjectRemovalResultEvent(Object source, Long projectId, ProjectRemovalStatus status, String message ) {
        super(source);
        this.message = message;
        this.status = status;
        this.projectId = projectId;

    }

    public static enum ProjectRemovalStatus {
        SUCCESS, FAIL
    }


}
