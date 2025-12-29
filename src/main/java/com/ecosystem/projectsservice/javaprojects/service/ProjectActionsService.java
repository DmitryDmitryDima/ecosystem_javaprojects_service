package com.ecosystem.projectsservice.javaprojects.service;

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
import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save.FileSaveEventChain;
import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save.FileSaveInfo;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryJDBCRepository;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectActionsUtils;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;


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
    private FileSaveEventChain fileSaveEventChain;


    @Value("${storage.system}")
    private String systemStoragePath;

    @Value("${storage.user}")
    private String userStoragePath;


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

        checks(securityContext, requestContext, projectId);

        // запись данных в редис с последующей генерацией ивента (если данных нет, их нужно создать)


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

                fileSaveEventChain.initChain(securityContext, requestContext, FileSaveInfo.builder()
                                .content(request.getContent())
                                .fileId(fileId)
                                .projectId(projectId)
                                .projectsPath(Path.of(userStoragePath,
                                        securityContext.getUuid().toString(),
                                        "projects").normalize().toString())
                        .build());

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
    private Project checks(SecurityContext securityContext, RequestContext requestContext, Long projectId) throws Exception{
        Optional<Project> projectCheck = projectRepository.findById(projectId);

        if (projectCheck.isEmpty()) throw new IllegalStateException("Проекта не существует");



        // todo проверка доступа к проекту

        return projectCheck.get();
    }


}
