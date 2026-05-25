package com.ecosystem.projectsservice.javaprojects.service.processes.files.file_removal;

import com.ecosystem.projectsservice.javaprojects.dto.projects.state.updates.ProjectStructureInvalidation;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.update.HotLayerUpdater;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.Compensator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;


@Component
public class FileRemovalChainCompensator implements Compensator<FileRemovalEvent> {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private HotLayerUpdater hotLayer;


    @Override
    public void compensation(FileRemovalEvent event) {

        String currentStep = event.getInternalData().getCurrentStep();

        if (currentStep.equals("blockFile")){
            transactionTemplate.execute(status -> {

                fileRepository.findByIdForUpdate(event.getExternalData().getFileId())
                        .ifPresentOrElse(file -> file.setStatus(FileStatus.AVAILABLE),
                                ()-> {throw new IllegalStateException("файла больше нет");}
                );

                return null;
            });
        }

        // мы должны инвалидировать структуру, чтобы она снова учитывала файл, снова ставший видимым
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
