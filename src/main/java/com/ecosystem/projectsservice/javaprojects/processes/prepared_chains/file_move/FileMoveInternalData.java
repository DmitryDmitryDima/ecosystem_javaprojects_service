package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_move;


import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.InternalEventData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileMoveInternalData extends InternalEventData {
    private String projectsPath;
    private Long projectRoot;
}
