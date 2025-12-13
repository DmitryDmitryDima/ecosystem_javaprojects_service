package com.ecosystem.projectsservice.javaprojects.service;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.model.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryJDBCRepository;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectActionsUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


// todo методы проверки могут быть оптимизированы кастомный join requests

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


    @Value("${storage.system}")
    private String systemStoragePath;

    @Value("${storage.user}")
    private String userStoragePath;


    // данный метод ориентируется на выброс исключений, перехватываемых в advice
    @Transactional// аннотация нужна, так как работаем с lazy
    public ProjectDTO readProject(SecurityContext securityContext, RequestContext requestContext, Long projectId) throws Exception{



        Project project = checks(securityContext, requestContext, projectId);

        // извлекаем все папки, принадлежащие проекту, вместе с зависимостями
        List<DirectoryReadOnly> directories = directoryJDBCRepository.loadAWholeStructureFromRoot(project.getRoot().getId());

        // извлекаем все файлы, принадлежащие проекту
        List<FileReadOnly> files = directoryJDBCRepository.loadFilesAssosiatedWithDirectories(
                directories.stream().map(DirectoryReadOnly::getId).toList()
        );


        return utils.generateProjectDTOWithStructure(project, directories, files);
    }





    // так как каждый запрос базируется на id, мы должны извлечь проект и провести базовую проверку по нему
    private Project checks(SecurityContext securityContext, RequestContext requestContext, Long projectId) throws Exception{
        Optional<Project> projectCheck = projectRepository.findById(projectId);

        if (projectCheck.isEmpty()) throw new IllegalStateException("Проекта не существует");



        // todo проверка доступа к проекту

        return projectCheck.get();
    }


}
