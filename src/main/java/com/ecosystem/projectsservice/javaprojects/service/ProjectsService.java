package com.ecosystem.projectsservice.javaprojects.service;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectBuildFromSystemTemplateInfo;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectCreationRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectRemovalRequest;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectStatus;
import com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template.ProjectBuildFromTemplateInfo;
import com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template.ProjectCreationFromSystemInstructionEventChain;
import com.ecosystem.projectsservice.javaprojects.processes.chains.project_removal.ProjectRemovalEventChain;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectsService {

    @Value("${storage.system}")
    private String systemStoragePath;

    @Value("${storage.user}")
    private String userStoragePath;

    // папка, где хранятся готовые инструкции для проекта. В будущем вполне возможно, что пользователь (или ai) сможет сам написать подобную инструкцию
    private final String INSTRUCTIONS_FOLDER = "build_instructions";
    private final String TEMPLATES_FOLDER = "file_templates";



    @Autowired
    private ProjectConstructor projectConstructor;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private FileRepository fileRepository;


    // цепочки событий
    @Autowired
    private ProjectRemovalEventChain removalEventChain;

    @Autowired
    private ProjectCreationFromSystemInstructionEventChain internalCreationEventChain;


    // todo приватность
    public List<ProjectDTO> getAllProjects(SecurityContext securityContext, String targetUsername){


        List<Project> projects = projectRepository.findByUserUUID(securityContext.getTargetUUID());


        return projects.stream().map(p->ProjectDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .status(p.getStatus())
                .build()).toList();
    }

    /*
    пока что удаление происходит безвозвратно, возможно на более поздних этапах разработки добавлю что-то вроде корзины
     */

    public void deleteProject(SecurityContext securityContext, ProjectRemovalRequest request){


        removalEventChain.initProjectRemovalChain(securityContext,request.getProjectId());



    }



    public void createProject(SecurityContext securityContext, RequestContext requestContext, ProjectCreationRequest projectCreationRequest){

        // todo если есть prompt - обращаемся к ai event chain, если нет - внутренний event chain
        ProjectBuildFromTemplateInfo info = ProjectBuildFromTemplateInfo.builder()
                .instructionsPath(Path.of(systemStoragePath, INSTRUCTIONS_FOLDER))
                .fileTemplatesPath(Path.of(systemStoragePath, TEMPLATES_FOLDER))
                .projectsPath(Path.of(userStoragePath, securityContext.getUuid().toString(), "projects"))
                .projectType(ProjectType.MAVEN_CLASSIC)
                .needEntryPoint(projectCreationRequest.getNeedEntryPoint())
                .build();

        internalCreationEventChain.initChain(securityContext, requestContext, info);




    }




    // todo пока что оставим в классическом виде, без ивентов. Версия с генерацией ИИ будет иметь event chain
    @Transactional(rollbackOn = Exception.class)
    public void createProjectFromSystemTemplate(SecurityContext securityContext, ProjectCreationRequest request) throws Exception{


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
        project.setStatus(ProjectStatus.AVAILABLE);

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
                .instructionsPath(Path.of(systemStoragePath, INSTRUCTIONS_FOLDER).normalize().toString())
                .fileTemplatesPath(Path.of(systemStoragePath, TEMPLATES_FOLDER).normalize().toString())
                .projectType(ProjectType.MAVEN_CLASSIC)
                .needEntryPoint(request.getNeedEntryPoint())
                .build();

        projectConstructor.buildProjectFromSystemTemplate(info);





    }


}
