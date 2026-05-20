package com.ecosystem.projectsservice.javaprojects.service.processes.files.file_move;

import com.ecosystem.projectsservice.javaprojects.dto.projects.state.ProjectStructureInvalidation;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.update.HotLayerUpdater;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.Compensator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;


@Component
public class FileMoveChainCompensator implements Compensator<FileMoveEvent> {

    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private HotLayerUpdater hotLayer;

    @Override
    public void compensation(FileMoveEvent event) {
        if (event.getInternalData().getCurrentStep().equals("preparing")
                || event.getInternalData().getCurrentStep().equals("block_entities") ||
                event.getInternalData().getCurrentStep().equals("db_parent_switch")
        ){
            transaction.execute(status -> {

                Optional<File> fileCheck = fileRepository.findById(event.getExternalData().getFileId());

                if (fileCheck.isEmpty()) throw new IllegalStateException("Файла не существует");

                fileCheck.get().setStatus(FileStatus.AVAILABLE);

                Optional<Directory> directory
                        = directoryRepository.findById(event.getExternalData().getParent());
                if (directory.isEmpty()) throw new IllegalStateException("Директории не существует");

                directory.get().setStatus(DirectoryStatus.AVAILABLE);



                return null;
            });
        };

        // кеш операции не означают остановки всего процесса
        try {
            hotLayer.projectStructureInvalidation(new ProjectStructureInvalidation(
                    event.getContext().getProjectId()
            ));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
