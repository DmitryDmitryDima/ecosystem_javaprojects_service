package com.ecosystem.projectsservice.javaprojects.service.processes.projects_processes.project_creation_from_template;


import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import com.ecosystem.projectsservice.javaprojects.service.external_values.StorageExternals;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.read.SnapshotService;
import com.ecosystem.projectsservice.javaprojects.service.storage.StorageService;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.Compensator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProjectCreationFromSystemInstructionCompensator
        implements Compensator<ProjectCreationFromSystemInstructionEvent> {




    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private StorageExternals storageExternals;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private StorageService storageService;


    @Override
    public void compensation(ProjectCreationFromSystemInstructionEvent event) {

        try {

            String step = event.getInternalData().getCurrentStep();

            if (!step.equals("project_entity_creation")){
                removeProjectAndAndCreatedObjects(event.getExternalData().getProjectId());
            }
        }

        catch (Exception e){
            // todo тут будет механизм обработки провалившейся компенсации
        }

    }


    // извлекаем uuid созданных объектов (файлов), после чего удаляем их из хранилища
    private void removeProjectAndAndCreatedObjects(UUID projectId){

        List<String> dbFiles = transactionTemplate.execute(status -> {


            Optional<Project> projectCheck = projectRepository.findById(projectId);

            if (projectCheck.isEmpty()) throw new IllegalStateException("missing project");

            Project project = projectCheck.get();

            List<String> files = null;

            // если директория была создана, то она может содержать созданные файлы,
            // наличие которых нужно проверить
            if (project.getRoot() != null){
                files = snapshotService.getAllFilesBelowDirectory(project.getRoot().getId())
                        .stream().map(entity->entity.getId().toString()).toList();
            }



            projectRepository.deleteById(projectId);

            return files;
        });


        storageService.deleteBatch(storageExternals.getStorageUserBucket(), dbFiles);
    }
}
