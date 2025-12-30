package com.ecosystem.projectsservice.javaprojects.processes.chains.project_removal;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_queue.UserEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_queue.UserEventContext;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectStatus;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

@Service
public class ProjectRemovalEventChain {

    private static final String resultingEventName = "java_project_removal";



    // набор зависимостей для работы с бд

    @Autowired
    private DirectoryRepository directoryRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private FileRepository fileRepository;


    // публикация ивентов при прохождении цепочки - в том числе ивентов, предназначенных для rabbit mq (result_event {status, message, id})
    @Autowired
    private ApplicationEventPublisher publisher;




    // на входе в цепочку мы проверяем сущность и блокируем ее специальным статусом
    @Async("taskExecutor")
    public void initProjectRemovalChain(SecurityContext securityContext, RequestContext requestContext, Long projectId, String projectsPath){

        UserEventContext sharedContext = UserEventContext.builder()
                .userUUID(securityContext.getUuid())
                .timestamp(Instant.now())
                .username(securityContext.getUsername())
                .correlationId(requestContext.getCorrelationId())
                .build();

        // собираем изначальный metadata для цепочки (если преокт найден - вставляем еще и имя)
        ProjectRemovalEventData eventData = ProjectRemovalEventData.builder()
                .projectId(projectId)
                .build();







        ProjectRemovalInitiationEvent event = new ProjectRemovalInitiationEvent(this, projectsPath);
        event.setData(eventData);
        event.setContext(sharedContext);












        // публикуем ивент
        publisher.publishEvent(event);







    }

    @EventListener
    @Transactional
    public void findAndBlockProject(ProjectRemovalInitiationEvent event){
        Project project;
        try {
            System.out.println(event.getData().getProjectId());

            Optional<Project> existenceCheck = projectRepository.findById(event.getData().getProjectId());

            if (existenceCheck.isEmpty()){

                sendFailedResult("Проект не найден", event.getContext(), event.getData());
                return;
            }
            project = existenceCheck.get();
        }
        catch (Exception e){
            sendFailedResult("Ошибка удаления. Причина: "+e.getMessage(), event.getContext(), event.getData());
            return;
        }


        // проект удалить может лишь автор проекта
        if (!project.getUserUUID().equals(event.getContext().getUserUUID())){

            sendFailedResult("Ошибка доступа", event.getContext(), event.getData());
            return;
        }

        if (project.getStatus()!= ProjectStatus.AVAILABLE){


            sendFailedResult("Неподходящий статус. Проверьте, запущен ли проект",
                    event.getContext(),
                    event.getData());
            return;
        }

        // блокируем проект специальным статусом
        project.setStatus(ProjectStatus.REMOVING);
        projectRepository.save(project);




        // формируем полный путь до проекта
        String path = Path.of(event.getProjectsPath(), project.getName()).toString();





        // формируем следующий ивент для цепочки
        ProjectRemovalProjectBlockedEvent blockEvent = new ProjectRemovalProjectBlockedEvent(this, path);

        // делаем инъекцию метаданных и контекста (не забываем добавить имя проекта)
        event.getData().setProjectName(project.getName());
        blockEvent.setContext(event.getContext());
        blockEvent.setEventData(event.getData());




        // публикуем ивент
        publisher.publishEvent(blockEvent);

    }


    // чистим диск
    @EventListener
    public void removeProjectFromDisk(ProjectRemovalProjectBlockedEvent blockEvent){

        try {
            FileSystemUtils.deleteRecursively(Path.of(blockEvent.getDiskPath()));

            ProjectRemovalCleanedDiskEvent projectRemovalCleanedDiskEvent = new ProjectRemovalCleanedDiskEvent(this);
            projectRemovalCleanedDiskEvent.setContext(blockEvent.getContext());
            projectRemovalCleanedDiskEvent.setEventData(blockEvent.getEventData());

            System.out.println("inside disk "+projectRemovalCleanedDiskEvent.getContext());

            publisher.publishEvent(projectRemovalCleanedDiskEvent);
        }
        catch (Exception e){

            sendFailedResult("Ошибка удаления с диска", blockEvent.getContext(), blockEvent.getEventData());
        }

    }

    // дочищаем бд
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void removeProjectFromDB(ProjectRemovalCleanedDiskEvent event){

        Optional<Project> projectCheck = projectRepository.findById(event.getEventData().getProjectId());
        if (projectCheck.isEmpty()){

            sendFailedResult("Ошибка удаления "+event.getEventData().getProjectName()+" проекта. Сущность отсутствует в бд",
                    event.getContext(), event.getEventData());
            return;
        }

        try {
            System.out.println(projectCheck.get().getId());
            projectRepository.delete(projectCheck.get());

        }
        catch (Exception e){

            sendFailedResult("Ошибка удаления "+event.getEventData().getProjectName()+" проекта. Причина: "+e.getMessage(),
                    event.getContext(), event.getEventData());
            return;
        }




        System.out.println("inside db "+event.getContext());
        // публикация result event для внешней системы

        sendSuccessResult("Проект "+event.getEventData().getProjectName()+" успешно стерт", event.getContext(), event.getEventData());


    }
    private void sendResult(String message, UserEventContext context, ProjectRemovalEventData data){

        try {
            UserEvent userEvent = UserEvent.builder()
                    .eventData(data)
                    .event_type(resultingEventName)
                    .context(context)
                    .message(message)
                    .build();


            Thread.sleep(5000); // симуляция долгой операции

            publisher.publishEvent(userEvent);
        }
        catch (Exception e){
            // ошибка тут должна компенсироваться в будущем
            e.printStackTrace();
        }
    }

    private void sendFailedResult(String message, UserEventContext context, ProjectRemovalEventData data){
        data.setStatus(ProjectRemovalStatus.FAIL);
        sendResult(message, context, data);
    }

    private void sendSuccessResult(String message, UserEventContext context, ProjectRemovalEventData data){
        data.setStatus(ProjectRemovalStatus.SUCCESS);
        sendResult(message, context, data);
    }

    // specific status event


















}
