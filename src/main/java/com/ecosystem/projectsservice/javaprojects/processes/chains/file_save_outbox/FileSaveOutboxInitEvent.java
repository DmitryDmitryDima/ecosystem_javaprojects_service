package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox;

import com.ecosystem.projectsservice.javaprojects.processes.chains.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.EventName;
import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save.FileSaveEventData;
import com.ecosystem.projectsservice.javaprojects.processes.external_queue.ProjectEventContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;


@Getter
@Setter
@EventName("outbox_file_save_init")
public class FileSaveOutboxInitEvent extends ChainEvent {





    // контекст - данные контекста пользователя и сущности, с которой он работает
    private ProjectEventContext context;

    // данные, которые передаются сквозь всю цепочку ивентов, могут обогащаться. Предназначены для передачи внешней очереди
    private FileSaveEventData data;


    private String projectsPath;

    public FileSaveOutboxInitEvent(Object source, String projectsPath) {
        super(source);
        this.projectsPath = projectsPath;
    }
}
