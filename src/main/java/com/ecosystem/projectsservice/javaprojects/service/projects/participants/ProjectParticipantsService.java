package com.ecosystem.projectsservice.javaprojects.service.projects.participants;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.InviteTokenValidationResponse;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.ProjectAddParticipantRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.ProjectRemoveParticipantRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.ProjectInviteCreationRequest;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.ProjectInviteToken;
import com.ecosystem.projectsservice.javaprojects.model.ProjectParticipant;
import com.ecosystem.projectsservice.javaprojects.model.enums.ParticipantRole;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectPrivacyLevel;
import com.ecosystem.projectsservice.javaprojects.transport.broadcast.Broadcast;
import com.ecosystem.projectsservice.javaprojects.transport.broadcast.BroadcastException;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.routing_strategies.AlarmAction;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.routing_strategies.AlarmStrategy;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.routing_strategies.NotificationStrategy;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;
import com.ecosystem.projectsservice.javaprojects.service.processes.broadcastable_events.ParticipantActionData;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectInviteTokenRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectParticipantRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
    private Broadcast broadcast;

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
                                         Project project)  {

        createParticipant(toAddUUID, project);

        try {
            broadcast.sendSync(new Broadcast.EventBuilder().useEvent(ProjectEventFromUser::new)

                    .withContext(()-> {
                        NotificationStrategy notificationStrategy = new NotificationStrategy();
                        // вставляем необходимые дополнения в контекст
                        // событие удаления проекта рассылается персонально участникам проекта и его автору, либо открытый, либо закрытый канал
                        List<UUID> toNotify = new ArrayList<>();
                        toNotify.add(project.getUserUUID());
                        toNotify.addAll(project.getParticipants().stream().map(ProjectParticipant::getUserUUID).toList());

                        if (project.getPrivacyLevel() == ProjectPrivacyLevel.OPEN) {
                            notificationStrategy.setPublicChannel(toNotify);
                        } else {
                            notificationStrategy.setPrivateChannel(toNotify);
                        }
                        return ProjectEventFromUserContext
                                .from(context, requestContext, project.getId(), notificationStrategy, null);})


                    .withData(()->new ParticipantActionData(toAddUUID))
                    .withType(ExternalEventType.JAVA_PROJECT_ADD_PARTICIPANT)
                    .withMessage("К проекту "+project.getName()+" добавлен пользователь "+toAddUsername)
                    .build());
        } catch (BroadcastException e) {
            e.printStackTrace();
        }



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
        if (project.getUserUUID().equals(request.getUserId())) throw new IllegalStateException("автор не может добавить сам себя");

        participantAddDecorator(securityContext, requestContext, request.getUserId(), request.getUsername(),  project);



    }

    private void removeParticipant(UUID toRemove, Project from){
        from.getParticipants().removeIf(projectParticipant -> projectParticipant.getUserUUID().equals(toRemove));
    }



    // политика - если пользователь - участник проекта. то он может удалить только сам себя. В противном случае это право принадлежит автору проекта
    @Transactional
    public void removeParticipantFromProject(SecurityContext securityContext,
                                             RequestContext requestContext, ProjectRemoveParticipantRequest request)  {


        Optional<Project> projectCheck = projectRepository.findById(request.getProjectId());

        if (projectCheck.isEmpty()) throw new IllegalStateException("проекта не существует");

        Project project = projectCheck.get();

        if (project.getUserUUID().equals(request.getUserId())) throw new IllegalStateException("автор не может удалить сам себя. Для этого нужно удалить проект");

        List<UUID> participants = project.getParticipants().stream().map(ProjectParticipant::getUserUUID).toList();

        if (!participants.contains(securityContext.getUuid())&& !securityContext.getUuid().equals(project.getUserUUID())){
            throw new IllegalStateException("Вы не являетесь участником проекта");
        }
        if (!participants.contains(request.getUserId())) {
            throw new IllegalStateException("Тот, кого пытаются удалить, не является участником проекта");
        }
        // todo тут все может измениться при усложнении политики полномочий
        if (participants.contains(securityContext.getUuid()) && !request.getUserId().equals(securityContext.getUuid())){
            throw new IllegalStateException("Будучи участником, вы не можете удалить другого участника");
        }


        removeParticipant(request.getUserId(), project);

        try {
            broadcast.sendSync(new Broadcast.EventBuilder().useEvent(ProjectEventFromUser::new)
                    .withContext(()-> {

                        // вставляем необходимые дополнения в контекст
                        // событие удаления участника рассылается персонально участникам проекта и его автору, либо открытый, либо закрытый канал
                        NotificationStrategy notificationStrategy = new NotificationStrategy();
                        List<UUID> toNotify = new ArrayList<>();
                        toNotify.add(project.getUserUUID()); // автор проекта
                        toNotify.addAll(project.getParticipants().stream().map(ProjectParticipant::getUserUUID).toList()); // участники проекта
                        toNotify.add(request.getUserId()); // тот, кого удаляют

                        if (project.getPrivacyLevel() == ProjectPrivacyLevel.OPEN) {
                            notificationStrategy.setPublicChannel(toNotify);
                        } else {
                            notificationStrategy.setPrivateChannel(toNotify);
                        }

                        // добавляем alarm стратегию - событие требует действий в слое уведомлений
                        AlarmStrategy alarmStrategy = new AlarmStrategy(List.of(request.getUserId()), AlarmAction.SESSION_CLOSE);




                        return ProjectEventFromUserContext.from(securityContext, requestContext,
                                project.getId(),
                                notificationStrategy,
                                alarmStrategy);

                    })
                    .withData(()->new ParticipantActionData(request.getUserId()))
                    .withType(ExternalEventType.JAVA_PROJECT_REMOVE_PARTICIPANT)
                    .withMessage("Из проекта "+project.getName()+" удален пользователь "+request.getUsername())
                    .build());
        } catch (BroadcastException e) {
            e.printStackTrace();
        }


    }
}
