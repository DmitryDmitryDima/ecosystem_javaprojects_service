package com.ecosystem.projectsservice.javaprojects.service.processes.files.file_removal;

import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
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

    }
}
