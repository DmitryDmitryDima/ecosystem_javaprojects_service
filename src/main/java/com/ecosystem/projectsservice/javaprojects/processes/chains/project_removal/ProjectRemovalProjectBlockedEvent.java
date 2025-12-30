package com.ecosystem.projectsservice.javaprojects.processes.chains.project_removal;

import com.ecosystem.projectsservice.javaprojects.processes.external_queue.UserEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

// ивент, запускающий первый шаг - удаление с диска
@Getter
@Setter
public class ProjectRemovalProjectBlockedEvent extends ApplicationEvent {

    private UserEventContext context;
    private ProjectRemovalEventData eventData;

    private String diskPath;

    public ProjectRemovalProjectBlockedEvent(Object source, String diskPath) {
        super(source);
        this.diskPath = diskPath;



    }
}
