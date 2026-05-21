package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_removal;

import com.ecosystem.projectsservice.javaprojects.dto.projects.state.ProjectStructureInvalidation;
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
public class DirectoryRemovalChainCompensator
        implements Compensator<DirectoryRemovalEvent> {


    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private HotLayerUpdater hotLayer;



    @Override
    public void compensation(DirectoryRemovalEvent event) {

        String step = event.getInternalData().getCurrentStep();

        if (step.equals("prepare_directory")
                || step.equals("block_directory")){
            transaction.execute(status -> {
                Optional<Directory> directoryCheck = directoryRepository
                        .findByIdForUpdate(event.getExternalData().getId());
                if (directoryCheck.isEmpty()){
                    throw new IllegalStateException("Директории нет");

                }

                directoryCheck.get().setStatus(DirectoryStatus.AVAILABLE);
                return null;
            });
        }

        try {
            // инвалидируем структуру
            hotLayer.projectStructureInvalidation(
                    new ProjectStructureInvalidation(event.getContext().getProjectId()));
        }

        catch (Exception e){

        }




    }
}
