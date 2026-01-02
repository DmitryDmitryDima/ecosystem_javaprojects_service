package com.ecosystem.projectsservice.javaprojects.processes.chains.project_removal;

import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.UserExternalEventContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

// ивент, запускающий первый шаг - удаление с диска
@Getter
@Setter
public class ProjectRemovalProjectBlockedEvent extends ApplicationEvent {

    private UserExternalEventContext context;
    private ProjectRemovalEventData eventData;

    private String diskPath;

    public ProjectRemovalProjectBlockedEvent(Object source, String diskPath) {
        super(source);
        this.diskPath = diskPath;



    }
}
