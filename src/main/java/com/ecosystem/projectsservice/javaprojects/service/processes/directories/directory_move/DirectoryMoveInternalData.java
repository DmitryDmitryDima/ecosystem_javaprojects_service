package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_move;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.InternalEventData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectoryMoveInternalData extends InternalEventData {

    private String projectsPath;
    private Long projectRoot;
    private String oldPath;
}
