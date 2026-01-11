package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox;

import com.ecosystem.projectsservice.javaprojects.processes.chains.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.EventName;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ProjectExternalEventContext;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@EventName("outbox_file_save_init")
public class FileSaveOutboxInitEvent extends ChainEvent {





    // контекст - данные контекста пользователя и сущности, с которой он работает
    private ProjectExternalEventContext context;

    // данные, которые передаются сквозь всю цепочку ивентов, могут обогащаться. Предназначены для передачи внешней очереди
    private FileSaveEventData data;


    private String projectsPath;

    public FileSaveOutboxInitEvent(String projectsPath) {
        this.projectsPath = projectsPath;
    }
}
