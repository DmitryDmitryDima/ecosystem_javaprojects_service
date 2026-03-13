package com.ecosystem.projectsservice.javaprojects.service.projects;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.DirectoryAddRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.FileAddRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.FileSaveRequest;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.context_categories.ProjectEventFromUserContext;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.directory_add.DirectoryAddExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_add.FileAddExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_removal.FileRemovalExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.filesave.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.directory_add.DirectoryAddEvent;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.directory_add.DirectoryAddInternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_add.FileAddEvent;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_add.FileAddInternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_removal.FileRemovalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_removal.FileRemovalInternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.filesave.FileSaveEvent;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.filesave.FileSaveInternalData;
import com.ecosystem.projectsservice.javaprojects.service.ExternalValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class ProjectActionsEventBuilder {


    @Autowired
    private ExternalValues externalValues;


    public FileRemovalEvent buildFileRemovalEvent(SecurityContext securityContext, RequestContext requestContext, Project project, Long fileId){
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


        externalData.setFileId(fileId);

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
                                            FileSaveRequest request, long fileId){
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
        externalData.setFileId(fileId);

        // не путать с uuid того, кто выполняет запрос - это могут быть разные люди
        externalData.setFileOwner(project.getUserUUID());


        mainEvent.setExternalData(externalData);

        return mainEvent;
    }





}
