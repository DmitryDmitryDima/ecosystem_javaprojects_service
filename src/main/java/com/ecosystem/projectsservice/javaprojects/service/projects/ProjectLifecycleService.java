package com.ecosystem.projectsservice.javaprojects.service.projects;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.*;
import com.ecosystem.projectsservice.javaprojects.model.Project;

import com.ecosystem.projectsservice.javaprojects.model.ProjectParticipant;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectPrivacyLevel;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.routing_strategies.NotificationStrategy;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.UserPersonalEventContext;
import com.ecosystem.projectsservice.javaprojects.service.processes.projects.project_creation_from_template.ProjectCreationFromTemplateChain;
import com.ecosystem.projectsservice.javaprojects.service.processes.projects.project_creation_from_template.ProjectCreationFromTemplateEvent;
import com.ecosystem.projectsservice.javaprojects.service.processes.projects.project_creation_from_template.ProjectCreationFromTemplateExternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.projects.project_creation_from_template.ProjectCreationFromTemplateInternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.projects.project_removal.ProjectRemovalChain;
import com.ecosystem.projectsservice.javaprojects.service.processes.projects.project_removal.ProjectRemovalEvent;
import com.ecosystem.projectsservice.javaprojects.service.processes.projects.project_removal.ProjectRemovalExternalData;
import com.ecosystem.projectsservice.javaprojects.service.processes.projects.project_removal.ProjectRemovalInternalData;
import com.ecosystem.projectsservice.javaprojects.repository.*;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
    private ProjectRemovalChain removalChain;

    @Autowired
    private ProjectCreationFromTemplateChain projectCreationFromTemplateChain;












    /*
    Если target uuid = caller uuid - читаем все проекты, открытые/приватные, плюс те, где target является участником
    Если нет, то caller видит все открытые проекты target, а также те, где он сам является участником
    можно пойти дальше, сделав разделение на авторские проекты и те, в которых юзер участвует
     */
    @Transactional
    // todo n+1 problem - делегируй фильтрацию для базы данных
    public AllTargetRelatedProjects getAllProjects(SecurityContext securityContext, String targetUsername){





        if (securityContext.getTargetUUID()==null){
            throw new IllegalStateException("missing target uuid. Check request details");
        }

        UUID targetUUID = securityContext.getTargetUUID();
        UUID callerUUID = securityContext.getUuid();
        // извлекаем проекты, где target uuid является автором
        List<Project> authorProjects = projectRepository.findByUserUUID(securityContext.getTargetUUID()).stream()

                .filter(authorProject->{

                    if (authorProject.getPrivacyLevel()== ProjectPrivacyLevel.OPEN) return true;

                    // если отличается тот. кто смотрит, и тот, кого смотрят
                    if (!targetUUID.equals(callerUUID)){

                        List<UUID> participants = authorProject.getParticipants()
                                .stream()
                                .map(ProjectParticipant::getUserUUID).toList();
                        // приватные проекты видны только тогда, когда запрашивающий является его участником
                        return participants.contains(callerUUID);
                    }

                    return true;
                })

                .toList();



        // извлекаем проекты, где target uuid является участником. Если это приватный проект,
        // то он отображает только в том случае, если caller тоже участник
        List<Project> targetAsParticipantProjects = callerUUID.equals(targetUUID)?projectRepository.readAllProjectsByParticipant(targetUUID)
                :
                projectRepository.readAllParticipantProjectsByDifferentTargetAndCaller(targetUUID,securityContext.getUuid());









        AllTargetRelatedProjects projects = new AllTargetRelatedProjects();

        projects.setAuthorProjects(
                authorProjects.stream()
                        .map(thirdParty->ProjectLightweightDTO.builder()
                                .id(thirdParty.getId())
                                .name(thirdParty.getName())
                                .author(thirdParty.getUserUUID())
                                .privacyLevel(thirdParty.getPrivacyLevel())
                                .status(thirdParty.getStatus())
                                .participants(thirdParty.getParticipants().stream().map(ProjectParticipant::getUserUUID).toList())
                                .build()).toList());

        projects.setParticipantProjects(
                targetAsParticipantProjects.stream()
                        .map(thirdParty->ProjectLightweightDTO.builder()
                                .id(thirdParty.getId())
                                .name(thirdParty.getName())
                                .author(thirdParty.getUserUUID())
                                .privacyLevel(thirdParty.getPrivacyLevel())
                                .status(thirdParty.getStatus())
                                .participants(thirdParty.getParticipants().stream().map(ProjectParticipant::getUserUUID).toList())
                                .build()).toList());



        return projects;
    }






    /*
    пока что удаление происходит безвозвратно, возможно на более поздних этапах разработки добавлю что-то вроде корзины
    удалить проект может только тот, кто его создал
     */
    // TODO можно ли тут, или в цепочке, использовать project access validator?
    public void deleteProject(SecurityContext securityContext,
                              RequestContext requestContext,
                              ProjectRemovalRequest request)
            throws Exception {

        ProjectRemovalEvent mainEvent = new ProjectRemovalEvent();

        ProjectEventFromUserContext projectContext = new ProjectEventFromUserContext();
        projectContext.setRenderId(requestContext.getRenderId());
        projectContext.setUsername(securityContext.getUsername());
        projectContext.setTimestamp(Instant.now());
        projectContext.setUserUUID(securityContext.getUuid());
        projectContext.setCorrelationId(requestContext.getCorrelationId());
        projectContext.setProjectId(request.getProjectId());


        /*
        UserPersonalEventContext context = new UserPersonalEventContext();
        context.setRenderId(requestContext.getRenderId());
        context.setUsername(securityContext.getUsername());
        context.setTimestamp(Instant.now());
        context.setUserUUID(securityContext.getUuid());
        context.setCorrelationId(requestContext.getCorrelationId());
        context.setOpened(true); // ивент виден всем

         */








        ProjectRemovalExternalData externalData = new ProjectRemovalExternalData();
        externalData.setProjectId(request.getProjectId());


        ProjectRemovalInternalData internalData = new ProjectRemovalInternalData();
        internalData.setProjectPath(Path.of(userStoragePath, securityContext.getUuid().toString(),"projects").toString());




        mainEvent.setContext(projectContext);
        mainEvent.setExternalData(externalData);
        mainEvent.setInternalData(internalData);


        removalChain.init(mainEvent);





    }



    public void createProject(SecurityContext securityContext, RequestContext requestContext,
                              ProjectCreationRequest projectCreationRequest) throws Exception {

        System.out.println(projectCreationRequest);

        ProjectCreationFromTemplateEvent mainEvent = new ProjectCreationFromTemplateEvent();

        ProjectCreationFromTemplateInternalData internalData = new ProjectCreationFromTemplateInternalData();
        internalData.setProjectType(ProjectType.MAVEN_CLASSIC);
        internalData.setProjectsPath(Path.of(userStoragePath, securityContext.getUuid().toString(), "projects").normalize().toString());
        internalData.setInstructionsPath(Path.of(systemStoragePath, INSTRUCTIONS_FOLDER).normalize().toString());
        internalData.setNeedEntryPoint(projectCreationRequest.isNeedEntryPoint());
        internalData.setPrivacyLevel(projectCreationRequest.getPrivacyLevel());
        internalData.setFileTemplatesPath(Path.of(systemStoragePath, TEMPLATES_FOLDER).normalize().toString());


        ProjectCreationFromTemplateExternalData externalData = new ProjectCreationFromTemplateExternalData();
        externalData.setName(projectCreationRequest.getName());

        UserPersonalEventContext context = new UserPersonalEventContext();
        context.setUsername(securityContext.getUsername());
        context.setTimestamp(Instant.now());
        context.setUserUUID(securityContext.getUuid());
        context.setCorrelationId(requestContext.getCorrelationId());
        context.setRenderId(requestContext.getRenderId());

        // если проект публичный, о его появлении узнает в том числе внешний наблюдатель страницы пользователя (списка его проектов)
        NotificationStrategy notificationStrategy = new NotificationStrategy();
        if (projectCreationRequest.getPrivacyLevel()==ProjectPrivacyLevel.OPEN){
            notificationStrategy.setPublicChannel(List.of(securityContext.getUuid()));
        }

        context.setNotificationStrategy(notificationStrategy);


        mainEvent.setContext(context);
        mainEvent.setExternalData(externalData);
        mainEvent.setInternalData(internalData);

        projectCreationFromTemplateChain.init(mainEvent);




    }







}
