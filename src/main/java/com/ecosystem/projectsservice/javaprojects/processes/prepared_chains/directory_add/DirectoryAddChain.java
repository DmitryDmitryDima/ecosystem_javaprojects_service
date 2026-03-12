package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.directory_add;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.StructureSnapshot;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.ExternalResultType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.Message;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.Next;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.OpeningStep;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_add.FileAddEvent;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.SnapshotService;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectActionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_ADD_DIRECTORY)
public class DirectoryAddChain extends ControlledOutboxChain<DirectoryAddEvent> {

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private ProjectActionsUtils actionsUtils;

    @Override
    protected ExternalEvent<? extends ExternalEventContext> bindResultingEvent() {
        return new ProjectEventFromUser();
    }

    @Override
    protected void setProcessAssociations(DirectoryAddEvent event) {

    }

    @Override
    public void catchEvent(DirectoryAddEvent event) {

    }

    @Override
    public void compensationStrategy(DirectoryAddEvent event) {

    }

    // директория блокируется на операции удаления и перемещения - статус generating
    @OpeningStep(name = "block_directory")
    @Next(name="create_db_entity")
    @Message
    public void blockDirectory(DirectoryAddEvent event){
        event.setMessage("Проверяем директорию");

        transaction().execute(status -> {

            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParentId());
            if (directoryCheck.isEmpty()) throw new IllegalStateException("директории не существует");

            Directory directory = directoryCheck.get();

            StructureSnapshot snapshot = snapshotService.getSnapshot(event.getInternalData().getProjectRoot());

            Optional<DirectoryReadOnly> presenceCheck = actionsUtils.findAvailableDirectory(snapshot, directory.getId());
            if (presenceCheck.isEmpty()) throw new IllegalStateException("Директория не относится к проекту или недоступна для записи");

            // нужно проверить, есть ли подпапки с таким же именем

            // данный статус блокирует операцию удаления и операцию перемещения
            directory.setStatus(DirectoryStatus.GENERATING);

            return null;
        });
    }
}
