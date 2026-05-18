package com.ecosystem.projectsservice.javaprojects.service.processes.files.file_add;

import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.Compensator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;


@Component
public class FileAddChainCompensator implements Compensator<FileAddEvent> {


    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private FileRepository fileRepository;

    @Override
    public void compensation(FileAddEvent event) {
        if (!event.getInternalData().getCurrentStep().equals("block_directory")){
            transaction.execute(status -> {

                Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParentId());
                if (directoryCheck.isEmpty()) throw new IllegalStateException("директории не существует");
                directoryCheck.get().setStatus(DirectoryStatus.AVAILABLE);

                return null;
            });
            if (event.getExternalData().getId()!=null){
                // удаляем созданную сущность, если она есть
                transaction.execute(status -> {
                    fileRepository.deleteById(event.getExternalData().getId());
                    return null;
                });
            }



        }
    }
}
