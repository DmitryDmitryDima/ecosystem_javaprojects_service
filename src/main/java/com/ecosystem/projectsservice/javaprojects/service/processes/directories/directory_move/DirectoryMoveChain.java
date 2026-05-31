package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_move;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.StructureSnapshot;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.updates.DirectoryMove;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.updates.ProjectStructureInvalidation;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.read.SnapshotService;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.update.HotLayerUpdater;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.communication.Message;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.events.ExternalResultType;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.*;

@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_DIRECTORY_MOVE)
public class DirectoryMoveChain extends ControlledOutboxChain<DirectoryMoveEvent> {







    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private SnapshotService snapshotService;


    @Autowired
    private DirectoryMoveChainCompensator compensator;


    @Autowired
    private HotLayerUpdater hotLayer;










    @Override
    protected ExternalEvent<? extends ExternalEventContext> bindResultingEvent() {
        return new ProjectEventFromUser();
    }

    @Override
    protected void setProcessAssociations(DirectoryMoveEvent event) {

    }

    @Override
    @Async("chainExecutor")
    @EventListener
    public void catchEvent(DirectoryMoveEvent event) {
        super.processEvent(event);
    }

    @Override
    public void compensationStrategy(DirectoryMoveEvent event) {
        compensator.compensation(event);
    }


    @OpeningStep(name = "preparing")
    @Next(name = "blocking")
    @Message
    public void preparing(DirectoryMoveEvent event){
        event.setMessage("готовим сущности");
        // todo в version bubbling, по идее, мы сохраняем в ивент текущии версии child и parent, после чего сравниваем их в следующих шагах
        // для этой цели готовим detached объект
        class MovePreparingResult{
            Directory parent;
            Directory child;
        }

        MovePreparingResult result = transaction().execute(status -> {
            MovePreparingResult checkResult = new MovePreparingResult();



            Optional<Directory> childCheck
                    = directoryRepository.findByIdForUpdate(event.getExternalData().getDirectoryId());
            if (childCheck.isEmpty())
                throw new IllegalStateException("Директории, которую вы собираетесь перемещать, нет");

            Directory child = childCheck.get();

            if (child.isImmutable() || child.isHidden())
                throw new IllegalStateException("Директорию нельзя переместить");
            if (child.getStatus()!= DirectoryStatus.AVAILABLE)
                throw new IllegalStateException("Директория недоступна в данный момент");
            checkResult.child = child;


            Optional<Directory> parentCheck
                    = directoryRepository.findByIdForUpdate(event.getExternalData().getParent());

            if (parentCheck.isEmpty())
                throw new IllegalStateException("Директории, которую в которую вы собираетесь перемещать, нет");

            Directory parent = parentCheck.get();

            if (child.getParent().getId().equals(parent.getId()))
                throw new IllegalStateException("Директория уже является прямым родителем");

            if (parent.isHidden())
                throw new IllegalStateException("В директорию нельзя переместить");
            if (parent.getStatus()!= DirectoryStatus.AVAILABLE)
                throw new IllegalStateException("Директория," +
                    " в которую вы перемещаете, недоступна в данный момент");
            checkResult.parent = parent;

            parent.setStatus(DirectoryStatus.PREPARING_FOR_GENERATING);
            child.setStatus(DirectoryStatus.PREPARING_FOR_MIGRATING);





            return checkResult;
        });



        // инвалидация кеша структуры

        try {
            hotLayer.projectStructureInvalidation(
                    new ProjectStructureInvalidation(event.getContext().getProjectId())
            );
        }


        catch (Exception e){

        }


    }


    @Step(name = "blocking")
    @Message
    @Next(name = "parent_switch")
    public void blocking(DirectoryMoveEvent event){
        event.setMessage("блокируем сущности");

        transaction().execute(status -> {

            Optional<Directory> childCheck
                    = directoryRepository.findByIdForUpdate(event.getExternalData().getDirectoryId());
            if (childCheck.isEmpty())
                throw new IllegalStateException("Директории, которую вы собираетесь перемещать, нет");

            Directory child = childCheck.get();


            if (child.getStatus()!= DirectoryStatus.PREPARING_FOR_MIGRATING)
                throw new IllegalStateException("Неподходящий статус для шага blocking");

            Optional<Directory> parentCheck
                    = directoryRepository.findByIdForUpdate(event.getExternalData().getParent());
            if (parentCheck.isEmpty())
                throw new IllegalStateException("директории, в которую вы собираетесь перемещать, больше нет");

            Directory parent = parentCheck.get();

            if (parent.getStatus()!=DirectoryStatus.PREPARING_FOR_GENERATING)
                throw new IllegalStateException("Неподходящий статус для шага blocking");


            // у ребенка проверяем верхних родителей. Не забываем проверить принадлежность к проекту

            boolean directoryFound = false;
            boolean rootFound = false;

            List<DirectoryReadOnly> childParents
                    = snapshotService.getParentsSnapshotDirectoriesOnly(child.getId());

            for (DirectoryReadOnly directoryReadOnly:childParents){
                if (directoryReadOnly.getId().equals(child.getId())){
                    directoryFound = true;
                }
                else {
                    if (directoryReadOnly.getId().equals(event.getInternalData().getProjectRoot())){
                        rootFound = true;
                    }

                    if (!directoryReadOnly.getId().equals(parent.getId()) && directoryReadOnly.getStatus()!=DirectoryStatus.AVAILABLE){
                        throw new IllegalStateException("родители перемещаемой вами директории затронуты другим процессом. "
                                +directoryReadOnly.getName()
                                +" "+directoryReadOnly.getStatus());
                    }
                }
            }

            if (!(rootFound && directoryFound))
                throw new IllegalStateException("директорию, которую вы собираетесь переместить, не является частью проекта");




            // у ребенка проверяем детей - нельзя, чтобы среди них оказался новый родитель - верх иерархии не может быть перемещен к себе же вниз
            List<DirectoryReadOnly> childChildren
                    = snapshotService.getChildrenSnapshotDirectoriesOnly(child.getId());

            for (DirectoryReadOnly directoryReadOnly:childChildren){
                if (directoryReadOnly.getId().equals(parent.getId()))
                    throw new IllegalStateException("Новый родитель является ребенком перемещаемой директории");

                if (!directoryReadOnly.getId().equals(child.getId()) && directoryReadOnly.getStatus()!=DirectoryStatus.AVAILABLE)
                    throw new IllegalStateException("Ветви перемещаемой вами директории заблокированы другим процессом");
            }

            // у нового родителя проверяем родителей - проверяем принадлежность к проекту + статусы предков
            List<DirectoryReadOnly> parentParents
                    = snapshotService.getParentsSnapshotDirectoriesOnly(parent.getId());

            directoryFound = false;
            rootFound = false;

            for (DirectoryReadOnly directoryReadOnly:parentParents){
                if (directoryReadOnly.getId().equals(parent.getId())){
                    directoryFound = true;
                }
                else {
                    if (directoryReadOnly.getId().equals(event.getInternalData().getProjectRoot())){
                        rootFound = true;
                    }
                    if (
                            directoryReadOnly.getStatus()==DirectoryStatus.MIGRATING ||
                                    directoryReadOnly.getStatus()==DirectoryStatus.PREPARING_FOR_MIGRATING ||
                            directoryReadOnly.getStatus()==DirectoryStatus.PREPARING_FOR_REMOVAL ||
                            directoryReadOnly.getStatus() == DirectoryStatus.REMOVING
                    ){
                        throw new IllegalStateException("родительская ветка нового родителя занята другим процессом");
                    }
                }
            }

            if (!(directoryFound && rootFound)) throw new IllegalStateException("Новый родитель - не часть проекта");

            // проверяем, нет ли одноименной директории среди прямых детей нового родителя

            List<DirectoryReadOnly> firstParentLayer = snapshotService.getChildrenSnapshotDirectoriesOnlyWithLevel(parent.getId(),
                    1L);

            if (firstParentLayer.stream().anyMatch(directoryReadOnly -> directoryReadOnly.getName().equals(child.getName()))){
                throw new IllegalStateException("папка с таким именем уже существует в новом родителе");
            }


            parent.setStatus(DirectoryStatus.GENERATING);
            child.setStatus(DirectoryStatus.MIGRATING);



            return null;
        });
    }

    @Step(name = "parent_switch")
    @Message
    @Next(name = "release")
    public void parentSwitch(DirectoryMoveEvent event){

        event.setMessage("Перестраиваем базу данных");

        transaction().execute(status -> {

            Optional<Directory> childCheck = directoryRepository
                    .findByIdForUpdate(event.getExternalData().getDirectoryId());
            if (childCheck.isEmpty())
                throw new IllegalStateException("Директории, которую вы собираетесь перемещать, нет");

            Directory child = childCheck.get();


            if (child.getStatus()!= DirectoryStatus.MIGRATING)
                throw new IllegalStateException("Неподходящий статус для шага parent_switch");

            Optional<Directory> parentCheck
                    = directoryRepository.findByIdForUpdate(event.getExternalData().getParent());
            if (parentCheck.isEmpty())
                throw new IllegalStateException("директории, в которую вы собираетесь перемещать, больше нет");

            Directory parent = parentCheck.get();

            if (parent.getStatus()!=DirectoryStatus.GENERATING)
                throw new IllegalStateException("Неподходящий статус для шага parent_switch");

            // меняем родителя
            child.setParent(parent);
            parent.getChildren().add(child);




            // мы должны перестроить constructed path для всех детей child, а также ему самому child

            StructureSnapshot belowChild
                    = snapshotService.getFullChildrenSnapshot(child.getId());


            Map<UUID, String> directoryConstructedPaths = new HashMap<>();

            // обновляем пути директорий
            belowChild.getDirectories().stream().sorted(Comparator.comparingLong(DirectoryReadOnly::getDepth))
                    .forEach(directoryReadOnly -> {
                if (directoryReadOnly.getId().equals(child.getId())){
                    directoryConstructedPaths.put(child.getId(),
                            Path.of(parent.getConstructedPath(), directoryReadOnly.getName()).normalize().toString());


                }
                else {
                    String newPath = Path.of(directoryConstructedPaths.get(directoryReadOnly.getParent_id()),
                            directoryReadOnly.getName()).normalize().toString();
                    directoryConstructedPaths.put(directoryReadOnly.getId(), newPath);
                }


            });

            List<Directory> toUpdate = directoryRepository.findAllById(directoryConstructedPaths.keySet());

            toUpdate.forEach(directory -> {
                directory.setConstructedPath(directoryConstructedPaths.get(directory.getId()));
            });

            // обновляем пути для файлов
            HashMap<UUID, String> files = new HashMap<>();

            belowChild.getFiles().forEach(fileReadOnly -> {

                String path = Path.of(directoryConstructedPaths
                        .get(fileReadOnly.getParent_id()), fileReadOnly.getName()+"."+fileReadOnly.getExtension()).normalize().toString();

                files.put(fileReadOnly.getId(), path);
            });

            List<File> filesToUpdate = fileRepository.findAllById(files.keySet());

            filesToUpdate.forEach(file -> {
                file.setConstructedPath(files.get(file.getId()));
            });












            return null;
        });

    }




    @EndingStep(name="release")
    public void release(DirectoryMoveEvent event){

        var movedFiles = transaction().execute(status -> {

                    Optional<Directory> childCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getDirectoryId());
                    if (childCheck.isEmpty())
                        throw new IllegalStateException("Директории-потомка не существует");

                    Directory child = childCheck.get();


                    if (child.getStatus() != DirectoryStatus.MIGRATING)
                        throw new IllegalStateException("Неподходящий статус для шага release");

                    Optional<Directory> parentCheck = directoryRepository.findByIdForUpdate(event.getExternalData().getParent());
                    if (parentCheck.isEmpty())
                        throw new IllegalStateException("Родительской директории не существует");


                    Directory parent = parentCheck.get();

                    if (parent.getStatus()!=DirectoryStatus.GENERATING){
                        throw new IllegalStateException("неподходящий статус для шага release");
                    }

                    parent.setStatus(DirectoryStatus.AVAILABLE);
                    child.setStatus(DirectoryStatus.AVAILABLE);



                    // извлекаем список файлов, чей путь изменился

                    return snapshotService.getAllFilesBelowDirectory(child.getId());

                });



        try {
            // инвалидация кеша структуры проекта
            hotLayer.projectStructureInvalidation(
                    new ProjectStructureInvalidation(event.getContext().getProjectId())
            );


            // сайд эффекты
            List<FileDTO> touchedFiles = movedFiles.stream().map(file->

                    FileDTO.builder()
                            .extension(file.getExtension())
                            .name(file.getName())
                            .constructedPath(file.getConstructed_path())
                            .id(file.getId())
                            .projectId(event.getContext().getProjectId())
                            .build()

                    ).toList();

            hotLayer.onDirectoryMove(
                    DirectoryMove.builder()
                            .correlationId(event.getContext().getCorrelationId())
                            .userId(event.getContext().getUserUUID())
                            .username(event.getContext().getUsername())
                            .touchedFiles(touchedFiles).build()
            );

        }


        catch (Exception e){

        }


        event.setMessage("Освобождаем сущности");



    }

}
