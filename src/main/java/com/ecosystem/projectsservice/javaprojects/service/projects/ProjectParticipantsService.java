package com.ecosystem.projectsservice.javaprojects.service.projects;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.InviteTokenValidationResponse;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.ProjectAddParticipantRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.ProjectInviteCreationRequest;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.ProjectInviteToken;
import com.ecosystem.projectsservice.javaprojects.model.ProjectParticipant;
import com.ecosystem.projectsservice.javaprojects.model.enums.ParticipantRole;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectPrivacyLevel;
import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.broadcastable_action.ActionExecutionException;
import com.ecosystem.projectsservice.javaprojects.processes.broadcastable_action.BroadcastableAction;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ProjectEventFromUserContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.ExternalEmptyData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectInviteTokenRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectParticipantRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectParticipantsService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectParticipantRepository projectParticipantRepository;
    @Autowired
    private ProjectInviteTokenRepository projectInviteTokenRepository;

    @Autowired
    private BroadcastableAction broadcast;

    /*
    создаем инвайт токен для приглашения в проект
     */
    @Transactional
    public UUID createInviteToken(SecurityContext securityContext,
                                  RequestContext requestContext,
                                  ProjectInviteCreationRequest request) throws Exception{

        // для начала проверяем, является ли создатель токена хозяином проекта

        Optional<Project> projectCheck = projectRepository.findById(request.getProjectId());

        if (projectCheck.isEmpty()) throw new IllegalStateException("проекта не существует");

        Project project = projectCheck.get();
        if (!project.getUserUUID().equals(securityContext.getUuid()))
            throw new IllegalStateException("вы не можете пригласить в проект, это может сделать только автор");

        ProjectInviteToken projectInviteToken = new ProjectInviteToken();
        projectInviteToken.setProject(project);
        projectInviteToken.setUserUUID(request.getUserUUID());

        projectInviteToken = projectInviteTokenRepository.saveAndFlush(projectInviteToken);

        // id токена и есть токен
        return projectInviteToken.getId();




    }

    @Transactional
    public InviteTokenValidationResponse validateInviteToken(SecurityContext securityContext,
                                                             RequestContext requestContext,
                                                             UUID proposedToken) throws Exception{



        Optional<ProjectInviteToken> tokenCheck = projectInviteTokenRepository.findByIdForUpdate(proposedToken);
        if (tokenCheck.isEmpty()){
            throw new IllegalStateException("токена не существует");
        }
        ProjectInviteToken token = tokenCheck.get();

        if (token.isUsed() || token.getExpiredAt().isBefore(Instant.now())) {
            throw new IllegalStateException("токен просрочен");
        }

        if (token.getUserUUID()!=null && !securityContext.getUuid().equals(token.getUserUUID())){
            throw new IllegalStateException("Приглашение не для вас");
        }
        if (securityContext.getUuid().equals(token.getProject().getUserUUID())){
            throw new IllegalStateException("вы - автор");
        }



        // используем токен
        token.setUsed(true);

        participantAddDecorator(securityContext,
                requestContext,
                securityContext.getUuid(),
                securityContext.getUsername(),
                token.getProject());



        return new InviteTokenValidationResponse(token.getProject().getId());

    }

    private void createParticipant(UUID user, Project project){


        ProjectParticipant participant = new ProjectParticipant();
        participant.setProject(project);
        project.getParticipants().add(participant);
        participant.setRole(ParticipantRole.WRITER);
        participant.setUserUUID(user);
    }

    private void participantAddDecorator(SecurityContext context,
                                         RequestContext requestContext,
                                         UUID toAddUUID,
                                         String toAddUsername,
                                         Project project) throws ActionExecutionException {

        broadcast.statelessAction(()->createParticipant(toAddUUID, project))
                .withContext(()-> ProjectEventFromUserContext.from(context, requestContext,project,true,
                        project.getPrivacyLevel()== ProjectPrivacyLevel.OPEN))
                .withData(ExternalEmptyData::new
                )
                .withEvent(ProjectEventFromUser::new)
                .withType(ExternalEventType.JAVA_PROJECT_ADD_PARTICIPANT)
                .withMessage("К проекту "+project.getName()+" добавлен пользователь "+toAddUsername)
                .execute();

    }




    /*
    добавить участника в проект

     */

    @Transactional
    public void addParticipantToProject(SecurityContext securityContext, RequestContext requestContext, ProjectAddParticipantRequest request)
            throws Exception{
        Optional<Project> projectCheck = projectRepository.findById(request.getProjectId());

        if (projectCheck.isEmpty()) throw new IllegalStateException("проекта не существует");

        Project project = projectCheck.get();
        if (!project.getUserUUID().equals(securityContext.getUuid())) throw new IllegalStateException("Только автор может добавлять в проект");

        if (project.getParticipants().stream().map(ProjectParticipant::getUserUUID).toList()
                .contains(request.getUserId())) throw new IllegalStateException("пользователь уже участвует в проекте");
        if (project.getUserUUID().equals(request.getProjectId())) throw new IllegalStateException("автор не может добавить сам себя");

        participantAddDecorator(securityContext, requestContext, request.getUserId(), request.getUsername(),  project);



    }
}
