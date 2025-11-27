package com.ecosystem.projectsservice.javaprojects.processes.events.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

// ивент, запускающий первый шаг - удаление с диска
@Getter
@Setter
public class ProjectRemovalInitEvent extends ApplicationEvent {

    private String diskPath;
    private Long projectId;
    public ProjectRemovalInitEvent(Object source, Long projectId, String diskPath) {
        super(source);
        this.diskPath = diskPath;
        this.projectId = projectId;

    }
}
