package com.ecosystem.projectsservice.javaprojects.service;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ConstructorSettingsForSystemTemplateBuild;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectCreationRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectRemovalRequest;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectStatus;
import com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template.ProjectBuildFromTemplateInfo;
import com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template.ProjectInternalCreationEventChain;
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
import java.util.List;
import java.util.Optional;

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
    private ProjectInternalCreationEventChain internalCreationEventChain;


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

    public void deleteProject(SecurityContext securityContext, RequestContext requestContext, ProjectRemovalRequest request){


        removalEventChain.initProjectRemovalChain(securityContext, requestContext, request.getProjectId());



    }



    public void createProject(SecurityContext securityContext, RequestContext requestContext, ProjectCreationRequest projectCreationRequest){

        // todo если есть prompt - обращаемся к ai event chain, если нет - внутренний event chain
        ProjectBuildFromTemplateInfo info = ProjectBuildFromTemplateInfo.builder()
                .instructionsPath(Path.of(systemStoragePath, INSTRUCTIONS_FOLDER).normalize().toString())
                .fileTemplatesPath(Path.of(systemStoragePath, TEMPLATES_FOLDER).normalize().toString())
                .projectsPath(Path.of(userStoragePath, securityContext.getUuid().toString(), "projects").normalize().toString())
                .projectType(ProjectType.MAVEN_CLASSIC)
                .needEntryPoint(projectCreationRequest.isNeedEntryPoint())
                .projectName(projectCreationRequest.getName())
                .build();

        internalCreationEventChain.initChain(securityContext, requestContext, info);




    }







}
