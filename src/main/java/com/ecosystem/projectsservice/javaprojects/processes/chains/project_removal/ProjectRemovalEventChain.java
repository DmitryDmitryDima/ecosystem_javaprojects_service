package com.ecosystem.projectsservice.javaprojects.processes.chains.project_removal;


import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.processes.queue.UserEvent;
import com.ecosystem.projectsservice.javaprojects.processes.queue.UserEventContext;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.File;
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
    public void initProjectRemovalChain(SecurityContext securityContext, Long projectId){
        UserEventContext sharedContext = UserEventContext.builder()
                .userUUID(securityContext.getUuid())
                .timestamp(Instant.now())
                .username(securityContext.getUsername())
                .build();

        // собираем изначальный metadata для цепочки (если преокт найден - вставляем еще и имя)
        ProjectRemovalEventData metadata = ProjectRemovalEventData.builder()
                .projectId(projectId)
                .build();



        Optional<Project> existenceCheck = projectRepository.findById(projectId);

        if (existenceCheck.isEmpty()){
            metadata.setStatus(ProjectRemovalStatus.FAIL);
            sendResult("Проект не найден", sharedContext, metadata);
            return;
        }

        Project project = existenceCheck.get();

        if (!project.getUserUUID().equals(securityContext.getUuid())){
            metadata.setStatus(ProjectRemovalStatus.FAIL);
            sendResult("Ошибка доступа", sharedContext, metadata);
            return;
        }

        if (project.getStatus()!= ProjectStatus.AVAILABLE){
            metadata.setStatus(ProjectRemovalStatus.FAIL);

            sendResult("Неподходящий статус. Проверьте, запущен ли проект", sharedContext, metadata);
            return;
        }

        // блокируем проект специальным статусом
        project.setStatus(ProjectStatus.REMOVING);
        projectRepository.save(project);





        String path = project.getRoot().getConstructedPath();





        // формируем следующий ивент для цепочки
        ProjectRemovalInitEvent initEvent = new ProjectRemovalInitEvent(this, path);

        // делаем инъекцию метаданных и контекста (не забываем добавить имя проекта)
        metadata.setProjectName(project.getName());
        initEvent.setContext(sharedContext);
        initEvent.setMetadata(metadata);




        // публикуем ивент
        publisher.publishEvent(initEvent);







    }


    // чистим диск
    @EventListener
    @Async("taskExecutor")
    public void removeProjectFromDisk(ProjectRemovalInitEvent initEvent){

        try {
            FileSystemUtils.deleteRecursively(Path.of(initEvent.getDiskPath()));
            Thread.sleep(1000); // симуляция долгой операции
            ProjectRemovalCleanedDiskEvent projectRemovalCleanedDiskEvent = new ProjectRemovalCleanedDiskEvent(this);
            projectRemovalCleanedDiskEvent.setContext(initEvent.getContext());
            projectRemovalCleanedDiskEvent.setMetadata(initEvent.getMetadata());

            System.out.println("inside disk "+projectRemovalCleanedDiskEvent.getContext());

            publisher.publishEvent(projectRemovalCleanedDiskEvent);
        }
        catch (Exception e){
            sendResult("Ошибка удаления с диска", initEvent.getContext(), initEvent.getMetadata());
        }

    }

    // дочищаем бд
    @Transactional
    @EventListener
    public void removeProjectFromDB(ProjectRemovalCleanedDiskEvent event){

        // по идее тут не может быть null, так как проверка уже была пройдена в первом этапе
        Project project = projectRepository.getReferenceById(event.getProjectId());


        // todo dfs удаление всех связанных сущностей



        Directory root = project.getRoot();

        ArrayList<Directory> directoriesToDelete = new ArrayList<>();

        ArrayList<Directory> dfsStack = new ArrayList<>();
        dfsStack.add(root);

        while (!dfsStack.isEmpty()){
            Directory next = dfsStack.removeLast();
            directoriesToDelete.add(next);

            dfsStack.addAll(next.getChildren());
        }

        for(Directory queuedDirectory:directoriesToDelete){

            for (File file:queuedDirectory.getFiles()){
                fileRepository.delete(file);
            }
            System.out.println(directoriesToDelete);
            directoryRepository.delete(queuedDirectory);
        }

        projectRepository.delete(project);


        System.out.println("inside db "+event.getContext());
        // публикация result event для внешней системы
        sendResult("Проект "+event.getMetadata().getProjectName()+" успешно стерт", event.getContext(), event.getMetadata());


    }

    private void sendResult(String message, UserEventContext context, ProjectRemovalEventData metadata){
        UserEvent userEvent = UserEvent.builder()
                .eventData(metadata)
                .event_type(resultingEventName)
                .context(context)
                .message(message)
                .build();
        publisher.publishEvent(userEvent);
    }

















}
