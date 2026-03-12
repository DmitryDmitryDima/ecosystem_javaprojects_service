package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_add;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.StructureSnapshot;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.SnapshotService;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectActionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_FILE_ADD)
public class FileAddChain extends ControlledOutboxChain<FileAddEvent> {

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
    protected void setProcessAssociations(FileAddEvent event) {

    }

    @Override
    @Async("taskExecutor")
    @EventListener
    public void catchEvent(FileAddEvent event) {
       super.processEvent(event);
    }

    @Override
    public void compensationStrategy(FileAddEvent event) {
        if (!event.getInternalData().getCurrentStep().equals("block_directory")){
            transaction().execute(status -> {

                Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParentId());
                if (directoryCheck.isEmpty()) throw new IllegalStateException("директории не существует");
                directoryCheck.get().setStatus(DirectoryStatus.AVAILABLE);

                return null;
            });
        }
    }

    // директория блокируется на операции удаления и перемещения - статус generating
    @OpeningStep(name = "block_directory")
    @Next(name="create_db_entity")
    @Message
    public void blockDirectory(FileAddEvent fileAddEvent){
        fileAddEvent.setMessage("Проверяем директорию");

        transaction().execute(status -> {

            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(fileAddEvent.getExternalData().getParentId());
            if (directoryCheck.isEmpty()) throw new IllegalStateException("директории не существует");

            Directory directory = directoryCheck.get();

            StructureSnapshot snapshot = snapshotService.getSnapshot(fileAddEvent.getInternalData().getProjectRoot());

            Optional<DirectoryReadOnly> presenceCheck = actionsUtils.findAvailableDirectory(snapshot, directory.getId());
            if (presenceCheck.isEmpty()) throw new IllegalStateException("Директория не относится к проекту или недоступна для записи");

            if (snapshot.getFiles().stream().anyMatch(fileReadOnly ->
                    fileReadOnly.getName().equals(fileAddEvent.getExternalData().getFilename())
                            && fileReadOnly.getExtension().equals(fileAddEvent.getExternalData().getExtension()))){

                throw new IllegalStateException("файл с таким именем уже существует в этой директории");

            }

            // данный статус блокирует операцию удаления и операцию перемещения
            directory.setStatus(DirectoryStatus.GENERATING);

            return null;
        });
    }

    @Step(name="create_db_entity")
    @Next(name = "write_file_to_disk")
    @Message
    public void createDbEntity(FileAddEvent event){
        event.setMessage("Создаем директорию");
        File created = transaction().execute(status -> {

            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParentId());

            if (directoryCheck.isEmpty()) throw new IllegalStateException("директории не существует");
            Directory directory = directoryCheck.get();

            File file = new File();
            file.setStatus(FileStatus.AVAILABLE);
            file.setName(event.getExternalData().getFilename());
            file.setExtension(event.getExternalData().getExtension());
            file.setCreatedAt(Instant.now());
            file.setUpdatedAt(Instant.now());
            file.setConstructedPath(Path.of(directory.getConstructedPath(),
                    event.getExternalData().getFilename()+"."+event.getExternalData().getExtension()).normalize().toString());

            directory.getFiles().add(file);
            file.setParent(directory);



            return file;
        }
        );

        event.getInternalData()
                .setFilepath(Path.of(event.getInternalData().getProjectsPath(),
                        created.getConstructedPath()).normalize().toString());
    }

    @Step(name = "write_file_to_disk")
    @Next(name = "release_directory")
    @MaxRetry(maxCount = 3)
    public void writeFileToDisk(FileAddEvent event){
        try {
            Files.createFile(Path.of(event.getInternalData().getFilepath()));
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка записи файла в диск "+e.getMessage());
        }
    }

    @EndingStep(name = "release_directory")
    public void release(FileAddEvent event){

        transaction().execute(status -> {

            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParentId());
            if (directoryCheck.isEmpty()) throw new IllegalStateException("директории не существует");
            directoryCheck.get().setStatus(DirectoryStatus.AVAILABLE);

            return null;
        });

    }



}
