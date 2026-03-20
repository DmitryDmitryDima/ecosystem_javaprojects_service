package com.ecosystem.projectsservice.javaprojects.service.projects;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.directories.DirectoryAddRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.directories.DirectoryMoveRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.directories.DirectoryRemovalRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files.FileAddRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files.FileMoveRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files.FileRemovalRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files.FileSaveRequest;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_move.DirectoryMoveEvent;
import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_move.DirectoryMoveExternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_move.DirectoryMoveInternalData;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;
import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add.DirectoryAddExternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_removal.DirectoryRemovalEvent;
import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_removal.DirectoryRemovalExternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_removal.DirectoryRemovalInternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.file_add.FileAddExternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.file_move.FileMoveEvent;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.file_move.FileMoveExternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.file_move.FileMoveInternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.file_removal.FileRemovalExternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.filesave.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add.DirectoryAddEvent;
import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add.DirectoryAddInternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.file_add.FileAddEvent;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.file_add.FileAddInternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.file_removal.FileRemovalEvent;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.file_removal.FileRemovalInternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.filesave.FileSaveEvent;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.filesave.FileSaveInternalData;
import com.ecosystem.projectsservice.javaprojects.service.ExternalValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class ProjectActionsEventBuilder {


    @Autowired
    private ExternalValues externalValues;


    public DirectoryMoveEvent buildDirectoryMoveEvent(SecurityContext securityContext,
                                                      RequestContext requestContext,
                                                      Project project,
                                                      DirectoryMoveRequest request
                                                      ){

        DirectoryMoveEvent directoryMoveEvent = new DirectoryMoveEvent();
        directoryMoveEvent.setMessage("Перемещаем директорию...");

        // ивент пересылается только подписчикам комнаты
        ProjectEventFromUserContext context = ProjectEventFromUserContext.from(securityContext,
                requestContext,
                project,
                null, null);

        DirectoryMoveInternalData internalData = new DirectoryMoveInternalData();
        internalData.setProjectRoot(project.getRoot().getId());
        internalData.setProjectsPath(Path.of(externalValues.getUserStoragePath(),
                project.getUserUUID().toString(),
                "projects").normalize().toString());

        DirectoryMoveExternalData externalData = new DirectoryMoveExternalData();

        externalData.setParent(request.getParentId());
        externalData.setDirectoryId(request.getDirectoryId());

        directoryMoveEvent.setContext(context);
        directoryMoveEvent.setInternalData(internalData);
        directoryMoveEvent.setExternalData(externalData);

        return directoryMoveEvent;
    }


    public FileMoveEvent buildFileMoveEvent(SecurityContext securityContext, RequestContext requestContext, Project project,
                                            FileMoveRequest request
                                            ){
        FileMoveEvent fileMoveEvent = new FileMoveEvent();
        fileMoveEvent.setMessage("Перемещаем файл в директорию");

        // ивент пересылается только подписчикам комнаты
        ProjectEventFromUserContext context = ProjectEventFromUserContext.from(securityContext,
                requestContext,
                project,
                null, null);

        FileMoveInternalData internalData = new FileMoveInternalData();
        internalData.setProjectRoot(project.getRoot().getId());
        internalData.setProjectsPath(Path.of(externalValues.getUserStoragePath(),
                project.getUserUUID().toString(),
                "projects").normalize().toString());

        FileMoveExternalData externalData = new FileMoveExternalData();

        externalData.setParent(request.getParentId());
        externalData.setFileId(request.getFileId());

        fileMoveEvent.setContext(context);
        fileMoveEvent.setInternalData(internalData);
        fileMoveEvent.setExternalData(externalData);

        return fileMoveEvent;



    }


    public DirectoryRemovalEvent buildDirectoryRemovalEvent(SecurityContext securityContext, RequestContext requestContext, Project project,
                                                            DirectoryRemovalRequest request){

        DirectoryRemovalEvent directoryRemovalEvent = new DirectoryRemovalEvent();
        directoryRemovalEvent.setMessage("Удаляем директорию");

        // ивент пересылается только подписчикам комнаты
        ProjectEventFromUserContext context = ProjectEventFromUserContext.from(securityContext,
                requestContext,
                project,
                null, null);

        DirectoryRemovalInternalData directoryRemovalInternalData = new DirectoryRemovalInternalData();
        directoryRemovalInternalData.setProjectRoot(project.getRoot().getId());
        directoryRemovalInternalData.setProjectsPath(Path.of(externalValues.getUserStoragePath(),
                project.getUserUUID().toString(),
                "projects").normalize().toString());

        DirectoryRemovalExternalData externalData = new DirectoryRemovalExternalData();
        externalData.setId(request.getDirectoryId());

        directoryRemovalEvent.setContext(context);
        directoryRemovalEvent.setInternalData(directoryRemovalInternalData);
        directoryRemovalEvent.setExternalData(externalData);
        return directoryRemovalEvent;

    }


    public FileRemovalEvent buildFileRemovalEvent(SecurityContext securityContext, RequestContext requestContext, Project project,
                                                  FileRemovalRequest request){
        FileRemovalEvent mainEvent = new FileRemovalEvent();
        mainEvent.setMessage("Удаляем файл");

        // ивент пересылается только подписчикам комнаты
        ProjectEventFromUserContext context = ProjectEventFromUserContext.from(securityContext,
                requestContext,
                project,
                null, null);


        mainEvent.setContext(context);

        FileRemovalInternalData internalData = new FileRemovalInternalData();


        internalData.setProjectRoot(project.getRoot().getId());
        internalData.setProjectsPath(Path.of(externalValues.getUserStoragePath(),
                project.getUserUUID().toString(),
                "projects").normalize().toString());




        mainEvent.setInternalData(internalData);

        FileRemovalExternalData externalData = new FileRemovalExternalData();


        externalData.setFileId(request.getFileId());

        // не путать с uuid того, кто выполняет запрос - это могут быть разные люди
        externalData.setFileOwner(project.getUserUUID());

        mainEvent.setExternalData(externalData);

        return mainEvent;
    }


    public DirectoryAddEvent buildDirectoryAddEvent(SecurityContext securityContext,
                                                    RequestContext requestContext,
                                                    Project project, DirectoryAddRequest request){

        DirectoryAddEvent event = new DirectoryAddEvent();

        ProjectEventFromUserContext context = ProjectEventFromUserContext.from(securityContext, requestContext, project,
                null,
                null);

        DirectoryAddExternalData externalData = new DirectoryAddExternalData();
        externalData.setParentId(request.getParentId());
        externalData.setName(request.getName());

        DirectoryAddInternalData internalData = new DirectoryAddInternalData();
        internalData.setProjectRoot(project.getRoot().getId());
        internalData.setProjectsPath(Path.of(externalValues.getUserStoragePath(),
                project.getUserUUID().toString(),
                "projects").normalize().toString());

        event.setContext(context);
        event.setInternalData(internalData);
        event.setExternalData(externalData);
        event.setMessage("создаем директорию");

        return event;

    }

    public FileAddEvent buildFileAddEvent(SecurityContext securityContext,
                                          RequestContext requestContext,
                                          Project project,
                                          FileAddRequest request){

        FileAddEvent fileAddEvent = new FileAddEvent();


        ProjectEventFromUserContext context = ProjectEventFromUserContext.from(securityContext, requestContext, project,
                null,
                null);


        FileAddExternalData externalData = new FileAddExternalData();
        externalData.setFilename(request.getFilename());
        externalData.setExtension(request.getExtension());
        externalData.setParentId(request.getParentId());

        FileAddInternalData internalData = new FileAddInternalData();
        internalData.setProjectsPath(Path.of(externalValues.getUserStoragePath(),
                project.getUserUUID().toString(),
                "projects").normalize().toString());

        internalData.setProjectRoot(project.getRoot().getId());

        fileAddEvent.setContext(context);
        fileAddEvent.setExternalData(externalData);
        fileAddEvent.setInternalData(internalData);
        fileAddEvent.setMessage("Создание файла "+request.getFilename()+"."+request.getExtension());

        return fileAddEvent;
    }


    public FileSaveEvent buildFileSaveEvent(SecurityContext securityContext, RequestContext requestContext, Project project,
                                            FileSaveRequest request){
        FileSaveEvent mainEvent = new FileSaveEvent();
        mainEvent.setMessage("Сохраняем файл...");

        // конструируем контект - комната проекта, ивент не требует дополнительных стратегий рассылки
        ProjectEventFromUserContext context = ProjectEventFromUserContext
                .from(securityContext, requestContext, project, null, null);

        mainEvent.setContext(context);

        // внутренние данные - необходима начальная папка проекта (для проверки принадлежности) и путь до проектов
        FileSaveInternalData internalData = new FileSaveInternalData();


        internalData.setProjectRoot(project.getRoot().getId());
        internalData.setProjectsPath(Path.of(externalValues.getUserStoragePath(),
                project.getUserUUID().toString(),
                "projects").normalize().toString());



        mainEvent.setInternalData(internalData);


        // внешние данные
        FileSaveExternalData externalData = new FileSaveExternalData();
        externalData.setContent(request.getContent());
        externalData.setFileId(request.getFileId());

        // не путать с uuid того, кто выполняет запрос - это могут быть разные люди
        externalData.setFileOwner(project.getUserUUID());


        mainEvent.setExternalData(externalData);

        return mainEvent;
    }





}
