package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add;

import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.SnapshotService;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectActionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
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
    @Async("taskExecutor")
    @EventListener
    public void catchEvent(DirectoryAddEvent event) {
        super.processEvent(event);
    }

    @Override
    public void compensationStrategy(DirectoryAddEvent event) {
        transaction().execute(status -> {
            Optional<Directory> parent = directoryRepository.findByIdForUpdate(event.getExternalData().getParentId());
            if (parent.isEmpty()) throw new IllegalStateException("missing parent");
            parent.get().setStatus(DirectoryStatus.AVAILABLE);



            if (event.getInternalData().getCurrentStep().equals("write_to_disk")){
                directoryRepository.deleteById(event.getExternalData().getId());
            }

            return null;
        });


    }

    @OpeningStep(name = "preparing")
    @Next(name = "block_directory")
    @Message
    public void preparingPhase(DirectoryAddEvent event){
        event.setMessage("Резервируем сущность");
        transaction().execute(status -> {
            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParentId());
            if (directoryCheck.isEmpty() || directoryCheck.get().getStatus()!=DirectoryStatus.AVAILABLE){
                throw new IllegalStateException("Директория занята другим процессом");

            }
            directoryCheck.get().setStatus(DirectoryStatus.PREPARING_FOR_GENERATING);
            return null;
        });
    }


    // директория блокируется на операции удаления и перемещения - статус generating
    @Step(name = "block_directory")
    @Next(name="create_db_entity")
    @Message
    public void blockDirectory(DirectoryAddEvent event){
        event.setMessage("Проверяем директорию");

        transaction().execute(status -> {

            Optional<Directory> directoryCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParentId());
            if (directoryCheck.isEmpty()) throw new IllegalStateException("директории не существует");

            Directory directory = directoryCheck.get();


            List<DirectoryReadOnly> parents = snapshotService.getParentsSnapshotDirectoriesOnly(event.getExternalData().getParentId());

            System.out.println(parents);


            boolean parentContains = false;
            boolean rootContains = false;

            for (DirectoryReadOnly directoryReadOnly:parents){

                if (directoryReadOnly.getId().equals(event.getExternalData().getParentId())){
                    if (directoryReadOnly.getStatus()!=DirectoryStatus.PREPARING_FOR_GENERATING){
                        // данный сценарий как правило означает сбой - например. если шаг каким то образом попал в цепь после компенсации
                        throw new IllegalStateException("Директория не была зарезервирована для блокировки");
                    }
                    parentContains = true;
                }
                else {

                    // данные статусы у вышестоящих директорий препятствуют созданию новой папки в нижестоящей директории
                    if (directoryReadOnly.getStatus()==DirectoryStatus.REMOVING
                            || directoryReadOnly.getStatus() == DirectoryStatus.MIGRATING
                            || directoryReadOnly.getStatus() == DirectoryStatus.PREPARING_FOR_MIGRATING
                            || directoryReadOnly.getStatus() == DirectoryStatus.PREPARING_FOR_REMOVAL

                    ){
                        throw new IllegalStateException("используемая папка заблокирована другим процессом");
                    }

                    // root
                    if (directoryReadOnly.getId().equals(event.getInternalData().getProjectRoot())){
                        rootContains = true;
                    }


                }







            }

            if (!(parentContains && rootContains)){
                throw new IllegalStateException("директория не принадлежит проекту");
            }

            List<DirectoryReadOnly> children = snapshotService.getChildrenSnapshotDirectoriesOnly(event.getExternalData().getParentId());

            if (children.stream().anyMatch(directoryReadOnly -> directoryReadOnly.getParent_id()
                    .equals(event.getExternalData().getParentId()) && directoryReadOnly.getName().equals(event.getExternalData().getName()))){
                throw new IllegalStateException("папка с таким именем уже существует");
            }




            // данный статус блокирует операцию удаления и операцию перемещения
            directory.setStatus(DirectoryStatus.GENERATING);

            return null;
        });
    }

    @Step(name = "create_db_entity")
    @Next(name = "write_to_disk")
    public void createDbEntity(DirectoryAddEvent event){
        Directory created = transaction().execute(status -> {

                    Optional<Directory> parentCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParentId());

                    if (parentCheck.isEmpty()) throw new IllegalStateException("директории не существует");
                    Directory parent = parentCheck.get();

                    Directory newDirectory = new Directory();
                    newDirectory.setStatus(DirectoryStatus.AVAILABLE);
                    newDirectory.setName(event.getExternalData().getName());
                    newDirectory.setCreatedAt(Instant.now());
                    newDirectory.setConstructedPath(Path.of(parent.getConstructedPath(), newDirectory.getName()).normalize().toString());
                    parent.getChildren().add(newDirectory);
                    newDirectory.setParent(parent);

                    return newDirectory;


                }


        );


        event.getInternalData().setFullPath(Path.of(event.getInternalData().getProjectsPath(),
                created.getConstructedPath()).normalize().toString());

        event.getExternalData().setId(created.getId());
    }

    @Step(name = "write_to_disk")
    @Next(name = "release_parent")
    public void writeToDisk(DirectoryAddEvent event){
        try {
            Files.createDirectory(Path.of(event.getInternalData().getFullPath()));
        }
        catch (Exception e){
            throw new IllegalStateException("Ошибка записи в диск "+e.getMessage());
        }
    }

    @EndingStep(name = "release_parent")
    @MaxRetry(maxCount = 3)
    public void releaseParent(DirectoryAddEvent event){
        transaction().execute(status -> {
            Optional<Directory> parent = directoryRepository.findByIdForUpdate(event.getExternalData().getParentId());
            if (parent.isEmpty()) throw new IllegalStateException("missing parent");
            parent.get().setStatus(DirectoryStatus.AVAILABLE);
           return null;
        });
    }


}
