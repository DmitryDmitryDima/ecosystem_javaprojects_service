package com.ecosystem.projectsservice.javaprojects.processes.chains.project_removal;

import com.ecosystem.projectsservice.javaprojects.processes.queue.UserEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

// ивент, запускающий первый шаг - удаление с диска
@Getter
@Setter
public class ProjectRemovalInitEvent extends ApplicationEvent {

    private UserEventContext context;
    private ProjectRemovalEventData metadata;

    private String diskPath;

    public ProjectRemovalInitEvent(Object source, String diskPath) {
        super(source);
        this.diskPath = diskPath;



    }
}
