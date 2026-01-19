package com.ecosystem.projectsservice.javaprojects.service.projects;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.ProjectCreationRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.ProjectLightweightDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.ProjectRemovalRequest;
import com.ecosystem.projectsservice.javaprojects.model.Project;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.UserPersonalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.project_creation_from_template.ProjectCreationFromTemplateChain;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.project_creation_from_template.ProjectCreationFromTemplateEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.ProjectCreationFromTemplateExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.project_creation_from_template.ProjectCreationFromTemplateInternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.project_removal.ProjectRemovalChain;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.project_removal.ProjectRemovalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.ProjectRemovalExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.project_removal.ProjectRemovalInternalData;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Service
public class ProjectLifecycleService {

    @Value("${storage.system}")
    private String systemStoragePath;

    @Value("${storage.user}")
    private String userStoragePath;

    // папка, где хранятся готовые инструкции для проекта. В будущем вполне возможно, что пользователь (или ai) сможет сам написать подобную инструкцию
    private final String INSTRUCTIONS_FOLDER = "build_instructions";
    private final String TEMPLATES_FOLDER = "file_templates";





    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private FileRepository fileRepository;




    @Autowired
    private ProjectRemovalChain removalChain;

    @Autowired
    private ProjectCreationFromTemplateChain projectCreationFromTemplateChain;






    // todo приватность
    public List<ProjectLightweightDTO> getAllProjects(SecurityContext securityContext, String targetUsername){


        List<Project> projects = projectRepository.findByUserUUID(securityContext.getTargetUUID());


        return projects.stream().map(p-> ProjectLightweightDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .status(p.getStatus())
                .build()).toList();
    }

    /*
    пока что удаление происходит безвозвратно, возможно на более поздних этапах разработки добавлю что-то вроде корзины
     */

    public void deleteProject(SecurityContext securityContext, RequestContext requestContext, ProjectRemovalRequest request)
            throws Exception {

        ProjectRemovalEvent mainEvent = new ProjectRemovalEvent();

        UserPersonalEventContext context = new UserPersonalEventContext();
        context.setRenderId(requestContext.getRenderId());
        context.setUsername(securityContext.getUsername());
        context.setTimestamp(Instant.now());
        context.setUserUUID(securityContext.getUuid());
        context.setCorrelationId(requestContext.getCorrelationId());

        ProjectRemovalExternalData externalData = new ProjectRemovalExternalData();
        externalData.setProjectId(request.getProjectId());


        ProjectRemovalInternalData internalData = new ProjectRemovalInternalData();
        internalData.setProjectPath(Path.of(userStoragePath, securityContext.getUuid().toString(),"projects").toString());




        mainEvent.setContext(context);
        mainEvent.setExternalData(externalData);
        mainEvent.setInternalData(internalData);

        removalChain.init(mainEvent);





    }



    public void createProject(SecurityContext securityContext, RequestContext requestContext, ProjectCreationRequest projectCreationRequest) throws Exception {



        ProjectCreationFromTemplateEvent mainEvent = new ProjectCreationFromTemplateEvent();

        ProjectCreationFromTemplateInternalData internalData = new ProjectCreationFromTemplateInternalData();
        internalData.setProjectType(ProjectType.MAVEN_CLASSIC);
        internalData.setProjectsPath(Path.of(userStoragePath, securityContext.getUuid().toString(), "projects").normalize().toString());
        internalData.setInstructionsPath(Path.of(systemStoragePath, INSTRUCTIONS_FOLDER).normalize().toString());
        internalData.setNeedEntryPoint(projectCreationRequest.isNeedEntryPoint());
        internalData.setFileTemplatesPath(Path.of(systemStoragePath, TEMPLATES_FOLDER).normalize().toString());

        ProjectCreationFromTemplateExternalData externalData = new ProjectCreationFromTemplateExternalData();
        externalData.setName(projectCreationRequest.getName());

        UserPersonalEventContext context = new UserPersonalEventContext();
        context.setUsername(securityContext.getUsername());
        context.setTimestamp(Instant.now());
        context.setUserUUID(securityContext.getUuid());
        context.setCorrelationId(requestContext.getCorrelationId());
        context.setRenderId(requestContext.getRenderId());

        mainEvent.setContext(context);
        mainEvent.setExternalData(externalData);
        mainEvent.setInternalData(internalData);

        projectCreationFromTemplateChain.init(mainEvent);




    }







}
