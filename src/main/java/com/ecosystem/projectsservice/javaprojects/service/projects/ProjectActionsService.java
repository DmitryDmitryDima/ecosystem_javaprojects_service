package com.ecosystem.projectsservice.javaprojects.service.projects;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.ProjectSnapshot;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.SimpleFileInfo;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.FileSaveRequest;
import com.ecosystem.projectsservice.javaprojects.model.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.EventStatus;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ProjectExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.markers.ProjectEvent;
import com.ecosystem.projectsservice.javaprojects.processes.filesave.FileSaveChain;
import com.ecosystem.projectsservice.javaprojects.processes.filesave.FileSaveEvent;
import com.ecosystem.projectsservice.javaprojects.processes.filesave.event_structure.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.filesave.event_structure.FileSaveInternalData;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryJDBCRepository;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import com.ecosystem.projectsservice.javaprojects.service.cache.FileContent;
import com.ecosystem.projectsservice.javaprojects.service.cache.FileContentCache;
import com.ecosystem.projectsservice.javaprojects.service.cache.FileContentCacheImpl;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectActionsUtils;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


// todo методы проверки могут быть оптимизированы кастомный join requests

// ответственность внешнего сервиса - проверка прав. ответственность асинхронных внутренних цепочек - внутренние операции с бд, диском и кешем

@Service
public class ProjectActionsService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ProjectActionsUtils utils;



    @Autowired
    private DirectoryJDBCRepository directoryJDBCRepository;



    @Autowired
    private FileContentCache<FileContent, Long> fileContentCache;


    @Autowired
    private FileSaveChain fileSaveChain;


    @Value("${storage.system}")
    private String systemStoragePath;

    @Value("${storage.user}")
    private String userStoragePath;

    @Autowired
    private ApplicationEventPublisher publisher;




    // данный метод ориентируется на выброс исключений, перехватываемых в advice
    @Transactional
    public ProjectDTO readProject(SecurityContext securityContext, RequestContext requestContext, Long projectId) throws Exception{



        Project project = checks(securityContext, requestContext, projectId);

        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setProjectType(project.getType());
        projectDTO.setStatus(project.getStatus());
        projectDTO.setName(project.getName());
        projectDTO.setAuthor(project.getUserUUID());


        return utils.generateStructureForDTO(project.getRoot().getId(), projectDTO, getProjectSnapshot(project.getRoot().getId()));
    }


    // механизм автосохранения не полагается на цепочку, так как работает только с redis
    @Transactional
    public void autosave(SecurityContext securityContext,
                         RequestContext requestContext,
                         Long projectId,
                         Long fileId,
                         FileSaveRequest request) throws Exception{



        Project project = checks(securityContext, requestContext, projectId);

        ProjectSnapshot snapshot = getProjectSnapshot(project.getRoot().getId());

        for (FileReadOnly fileReadOnly:snapshot.getFiles()){

            if (fileReadOnly.getId().equals(fileId)){
                if (fileReadOnly.isHidden() || !fileReadOnly.getStatus().equals(FileStatus.AVAILABLE)){

                    throw new IllegalStateException("Файл не доступен для записи");
                }
            }
        }


        // пишем в кеш
        fileContentCache.save(FileContent.builder()
                        .id(fileId)
                        .text(request.getContent())
                .build());

        // публикуем ивент напрямую, без задействования outbox цепочек
        ProjectEvent projectEvent = new ProjectEvent();
        ProjectExternalEventContext externalEventContext = new ProjectExternalEventContext();
        externalEventContext.setProjectId(projectId);
        externalEventContext.setRenderId(requestContext.getRenderId());
        externalEventContext.setCorrelationId(requestContext.getCorrelationId());
        externalEventContext.setTimestamp(Instant.now());
        externalEventContext.setUsername(securityContext.getUsername());
        externalEventContext.setUserUUID(securityContext.getUuid());

        projectEvent.setContext(externalEventContext);

        FileSaveExternalData externalData = new FileSaveExternalData();
        externalData.setFileId(fileId);
        externalData.setContent(request.getContent());

        projectEvent.setStatus(EventStatus.SUCCESS);
        projectEvent.setType("java_project_file_save");



        publisher.publishEvent(projectEvent);









    }

    @Transactional
    public void saveFile(SecurityContext securityContext,
                         RequestContext requestContext,
                         Long projectId,
                         Long fileId,
                         FileSaveRequest request) throws Exception {

        Project project = checks(securityContext, requestContext, projectId);

        ProjectSnapshot snapshot = getProjectSnapshot(project.getRoot().getId());

        for (FileReadOnly fileReadOnly:snapshot.getFiles()){

            if (fileReadOnly.getId().equals(fileId)){
                if (fileReadOnly.isHidden() || !fileReadOnly.getStatus().equals(FileStatus.AVAILABLE)){

                    throw new IllegalStateException("Файл не доступен для записи");
                }

                FileSaveEvent mainEvent = new FileSaveEvent();
                mainEvent.setMessage("Сохраняем файл...");
                ProjectExternalEventContext context = ProjectExternalEventContext.builder()
                        .correlationId(requestContext.getCorrelationId())
                        .participants(List.of())
                        .projectId(projectId)
                        .renderId(requestContext.getRenderId())
                        .timestamp(Instant.now())
                        .username(securityContext.getUsername())
                        .userUUID(UUID.randomUUID())
                        .build();

                mainEvent.setContext(context);

                FileSaveInternalData internalData = new FileSaveInternalData();
                internalData.setProjectsPath(Path.of(userStoragePath,
                        securityContext.getUuid().toString(),
                        "projects").normalize().toString());
                mainEvent.setInternalData(internalData);

                FileSaveExternalData externalData = new FileSaveExternalData();
                externalData.setContent(request.getContent());
                externalData.setFileId(fileId);



                mainEvent.setExternalData(externalData);

                fileSaveChain.init(mainEvent);







                return;

            }
        }

        throw new IllegalStateException("Файл не найден в проекте, проверьте актуальность данных");




    }

    // todo внутри данной функции должен быть добавлен запрос участников проекта вместе с ролями (admin, author, viewer)
    private ProjectSnapshot getProjectSnapshot(Long rootId){
        // извлекаем все папки, принадлежащие проекту, вместе с зависимостями
        List<DirectoryReadOnly> directories = directoryJDBCRepository.loadAWholeStructureFromRoot(rootId);
        // извлекаем все файлы, принадлежащие проекту
        List<FileReadOnly> files = directoryJDBCRepository.loadFilesAssosiatedWithDirectories(
                directories.stream().map(DirectoryReadOnly::getId).toList()
        );
        return ProjectSnapshot.builder()
                .directories(directories)
                .files(files)
                .build();
    }

    @Transactional
    public List<SimpleFileInfo> getRecentFiles(SecurityContext securityContext, RequestContext requestContext, Long projectId) throws Exception {
        Project project = checks(securityContext, requestContext, projectId);

        ProjectSnapshot snapshot = getProjectSnapshot(project.getRoot().getId());

        return utils.getRecentFiles(snapshot);



    }


    @Transactional
    public FileDTO readFile(SecurityContext securityContext, RequestContext requestContext, Long projectId, Long fileId) throws Exception{
        Project project = checks(securityContext, requestContext, projectId);

        ProjectSnapshot snapshot = getProjectSnapshot(project.getRoot().getId());

        for (FileReadOnly fileReadOnly:snapshot.getFiles()){
            if (fileReadOnly.getId().equals(fileId)){
                if (fileReadOnly.isHidden()){
                    throw new IllegalStateException("Файл не доступен для чтения");
                }

                FileDTO fileDTO = new FileDTO();
                fileDTO.setName(fileReadOnly.getName());
                fileDTO.setExtension(fileReadOnly.getExtension());
                fileDTO.setLastUpdate(fileReadOnly.getUpdated_at());
                fileDTO.setConstructedPath(fileReadOnly.getConstructed_path());
                try {
                    Path path = ProjectUtils.constructPathToFile(userStoragePath, project, fileReadOnly.getConstructed_path());
                    fileDTO.setContent(ProjectUtils.readFile(path));

                }
                catch (Exception e){
                    throw new IllegalStateException("Ошибка чтения файла. Причина: "+e.getMessage());
                }

                return fileDTO;

            }
        }

        throw new IllegalStateException("файл не найден в проекте");
    }





    // так как каждый запрос базируется на id, мы должны извлечь проект и провести базовую проверку по нему

    // todo это можно кешировать с помощью ограниченного токена
    // todo тут же извлекается лист участников проекта
    private Project checks(SecurityContext securityContext, RequestContext requestContext, Long projectId) throws Exception{
        Optional<Project> projectCheck = projectRepository.findById(projectId);

        if (projectCheck.isEmpty()) throw new IllegalStateException("Проекта не существует");



        // todo проверка доступа к проекту

        return projectCheck.get();
    }


}
