package com.ecosystem.projectsservice.javaprojects.service.processes.files.file_removal;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.Compensator;
import org.springframework.stereotype.Component;


@Component
public class FileRemovalChainCompensator implements Compensator<FileRemovalEvent> {
    @Override
    public void compensation(FileRemovalEvent event) {

    }
}
