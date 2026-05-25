package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_move;

import com.ecosystem.projectsservice.javaprojects.dto.projects.state.updates.ProjectStructureInvalidation;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.update.HotLayerUpdater;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.Compensator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;


@Component
public class DirectoryMoveChainCompensator implements Compensator<DirectoryMoveEvent> {

    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private DirectoryRepository directoryRepository;


    @Autowired
    private HotLayerUpdater hotLayer;


    @Override
    public void compensation(DirectoryMoveEvent event) {
        String step = event.getInternalData().getCurrentStep();

        if (!step.equals("preparing")){
            transaction.execute(status -> {

                Optional<Directory> childCheck
                        = directoryRepository.findByIdForUpdate(event.getExternalData().getDirectoryId());
                if (childCheck.isEmpty())
                    throw new IllegalStateException("Директории, которую вы собирались перемещать, нет");

                childCheck.get().setStatus(DirectoryStatus.AVAILABLE);

                Optional<Directory> parentCheck
                        = directoryRepository.findByIdForUpdate(event.getExternalData().getParent());
                if (parentCheck.isEmpty())
                    throw new IllegalStateException("Директории, которую в которую вы собирались перемещать, нет");

                Directory parent = parentCheck.get();

                parent.setStatus(DirectoryStatus.AVAILABLE);


                return null;
            });
        }


        try {
            hotLayer.projectStructureInvalidation(
                    new ProjectStructureInvalidation(event.getContext().getProjectId())
            );
        }
        catch (Exception e){

        }
    }
}
