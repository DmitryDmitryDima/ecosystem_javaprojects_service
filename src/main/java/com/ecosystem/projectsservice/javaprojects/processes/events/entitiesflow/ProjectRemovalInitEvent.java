package com.ecosystem.projectsservice.javaprojects.processes.events.entitiesflow;

import com.ecosystem.projectsservice.javaprojects.processes.events.UserEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

// ивент, запускающий первый шаг - удаление с диска
@Getter
@Setter
public class ProjectRemovalInitEvent extends ApplicationEvent {

    private UserEventContext context;
    private String diskPath;
    private Long projectId;
    public ProjectRemovalInitEvent(Object source, Long projectId, String diskPath, UserEventContext context) {
        super(source);
        this.diskPath = diskPath;
        this.projectId = projectId;
        this.context = context;

    }
}
