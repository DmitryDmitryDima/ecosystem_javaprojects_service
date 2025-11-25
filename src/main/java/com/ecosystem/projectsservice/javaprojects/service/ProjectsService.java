package com.ecosystem.projectsservice.javaprojects.service;

import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectBuildFromSystemTemplateInfo;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectCreationRequest;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

@Service
public class ProjectsService {

    @Value("${storage.system}")
    private String systemStoragePath;

    @Value("${storage.user}")
    private String userStoragePath;

    // папка, где хранятся готовые инструкции для проекта. В будущем вполне возможно, что пользователь (или ai) сможет сам написать подобную инструкцию
    private final String INSTRUCTIONS_FOLDER = "build_instructions";



    @Autowired
    private ProjectConstructor projectConstructor;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private FileRepository fileRepository;


    @Transactional(rollbackOn = Exception.class)
    public void createProject(SecurityContext securityContext, ProjectCreationRequest request) throws Exception{


        // шаг 1 - смотрим все проекты пользователя и проверяем, есть ли среди них одноименный

        Optional<Project> projectWithName = projectRepository.findByNameAndUserUUID(request.getName(), securityContext.getUuid());

        if (projectWithName.isPresent()){
            throw new IllegalStateException("проект с таким именем уже существует");
        }

        System.out.println("project creation start");

        // шаг 2 - создаем entity
        Project project = new Project();
        project.setCreatedAt(Instant.now());
        project.setUserUUID(securityContext.getUuid());
        project.setName(request.getName());

        // шаг 3 - сохраняем основные сущности

        String path = Path.of(userStoragePath, securityContext.getUuid().toString(),"projects", project.getName())
                        .normalize()
                .toString();

        Directory root = new Directory();

        root.setCreatedAt(Instant.now());
        root.setImmutable(true); // корневая папка строго иммутабельна
        root.setName(project.getName());
        root.setConstructedPath(path);

        directoryRepository.save(root);

        project.setRoot(root);

        projectRepository.save(project);

        // шаг 4 - формируем информацию для постройки проекта и строим его todo тип пока один
        ProjectBuildFromSystemTemplateInfo info = ProjectBuildFromSystemTemplateInfo.builder()

                .project(project)
                .instructionPath(Path.of(systemStoragePath, "build_instructions", "maven_classic.yaml").normalize().toString())
                .fileTemplatesPath(Path.of(systemStoragePath, "file_templates").normalize().toString())
                .build();

        projectConstructor.buildProjectFromSystemTemplate(info);


































    }


}
