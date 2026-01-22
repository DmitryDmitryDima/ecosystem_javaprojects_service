package com.ecosystem.projectsservice.javaprojects.processes.process_control;

import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProjectAssociatedProcess extends ChainProcess {

    private Long projectId;
    private Long fileId;

    public ProjectAssociatedProcess(UUID correlationId, ExternalEventType type, String firstStep, Long projectId){
        super(correlationId, type, firstStep);
        this.projectId = projectId;

    }

    public ProjectAssociatedProcess(UUID correlationId, ExternalEventType type, String firstStep,  Long projectId, Long fileId){
        super(correlationId, type, firstStep);
        this.projectId = projectId;
        this.fileId = fileId;



    }
}
