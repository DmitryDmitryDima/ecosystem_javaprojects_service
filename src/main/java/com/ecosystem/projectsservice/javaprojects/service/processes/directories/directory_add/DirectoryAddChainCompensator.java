package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add;

import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.Compensator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@Component
public class DirectoryAddChainCompensator implements Compensator<DirectoryAddEvent> {

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private TransactionTemplate transaction;


    @Override
    public void compensation(DirectoryAddEvent event) {
        transaction.execute(status -> {

            // освобождаем родителя
            Optional<Directory> parent
                    = directoryRepository.findByIdForUpdate(event.getExternalData().getParentId());
            if (parent.isEmpty()) throw new IllegalStateException("отсутствует родитель");
            parent.get().setStatus(DirectoryStatus.AVAILABLE);





            return null;
        });
    }
}
