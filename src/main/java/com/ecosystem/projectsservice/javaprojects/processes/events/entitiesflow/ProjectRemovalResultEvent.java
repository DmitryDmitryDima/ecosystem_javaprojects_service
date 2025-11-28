package com.ecosystem.projectsservice.javaprojects.processes.events.entitiesflow;

import com.ecosystem.projectsservice.javaprojects.processes.events.UserEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.events.status.ProjectRemovalStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
@ToString
public class ProjectRemovalResultEvent extends ApplicationEvent {



    private ProjectRemovalStatus status;
    private String message;
    private Long projectId;
    private UserEventContext context;


    public ProjectRemovalResultEvent(Object source, Long projectId, ProjectRemovalStatus status, String message ) {
        super(source);
        this.message = message;
        this.status = status;
        this.projectId = projectId;

    }






}
