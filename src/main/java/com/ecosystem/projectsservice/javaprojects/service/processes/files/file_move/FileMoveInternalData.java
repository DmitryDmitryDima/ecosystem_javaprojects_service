package com.ecosystem.projectsservice.javaprojects.service.processes.files.file_move;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.InternalEventData;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FileMoveInternalData extends InternalEventData {
    //private String projectsPath;
    private UUID projectRoot;
    //private String oldPath;
    //private UUID projectOwner;
}
