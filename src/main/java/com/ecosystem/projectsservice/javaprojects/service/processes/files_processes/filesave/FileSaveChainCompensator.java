package com.ecosystem.projectsservice.javaprojects.service.processes.files_processes.filesave;

import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.Compensator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@Component
public class FileSaveChainCompensator implements Compensator<FileSaveEvent> {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private FileRepository fileRepository;


    @Override
    public void compensation(FileSaveEvent event) {
        String step = event.getInternalData().getCurrentStep();


        // нужно освободить файл. Примечание - файл не может быть изменен. если какой либо процесс занимает лок
        if (!step.equals("prepareFile")){
            transactionTemplate.execute(status -> {
                Optional<File> fileCheck = fileRepository
                        .findByIdForUpdate(event.getExternalData().getFileId());

                fileCheck.ifPresent(file -> file.setStatus(FileStatus.AVAILABLE));

                return null;
            });
        }
    }
}
