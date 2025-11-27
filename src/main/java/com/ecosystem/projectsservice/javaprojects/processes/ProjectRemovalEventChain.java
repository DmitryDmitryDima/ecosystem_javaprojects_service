package com.ecosystem.projectsservice.javaprojects.processes;


import com.ecosystem.projectsservice.javaprojects.message_queue.events_for_queue.ProjectRemovalResultEvent;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectStatus;
import com.ecosystem.projectsservice.javaprojects.processes.events.entities.ProjectRemovalCleanedDiskEvent;
import com.ecosystem.projectsservice.javaprojects.processes.events.entities.ProjectRemovalInitEvent;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Path;
import java.util.*;

@Service
public class ProjectRemovalEventChain {



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

    public void initRemovalChain(UUID userUUID, Long projectId){

        Optional<Project> existenceCheck = projectRepository.findById(projectId);
        if (existenceCheck.isEmpty()){
            sendFailedResult(projectId, "преокт не найден");
            return;
        }

        Project project = existenceCheck.get();

        if (!project.getUserUUID().equals(userUUID)){
            sendFailedResult(projectId,"ошибка доступа");
            return;
        }

        if (project.getStatus()!= ProjectStatus.AVAILABLE){
            sendFailedResult(projectId, "Неподходящий статус. Проверьте, запущен ли проект");
            return;
        }

        // блокируем проект специальным статусом
        project.setStatus(ProjectStatus.REMOVING);
        projectRepository.save(project);

        String path = project.getRoot().getConstructedPath();

        ProjectRemovalInitEvent initEvent = new ProjectRemovalInitEvent(this, projectId, path);




        publisher.publishEvent(initEvent);







    }


    // чистим диск
    @EventListener
    @Async("taskExecutor")
    public void removeProjectFromDisk(ProjectRemovalInitEvent initEvent){

        try {
            FileSystemUtils.deleteRecursively(Path.of(initEvent.getDiskPath()));
            Thread.sleep(1000); // симуляция долгой операции
            ProjectRemovalCleanedDiskEvent projectRemovalCleanedDiskEvent = new ProjectRemovalCleanedDiskEvent(this, initEvent.getProjectId());


            publisher.publishEvent(projectRemovalCleanedDiskEvent);
        }
        catch (Exception e){
            sendFailedResult(initEvent.getProjectId(),
                    "ошибка удаления файловых ресурсов");

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



        // публикация result event для внешней системы
        sendSuccessResult(project.getId(),"проект успешно стерт");


    }


    private void sendSuccessResult(Long projectId,  String message){

        ProjectRemovalResultEvent removalResultEvent = new ProjectRemovalResultEvent(this, projectId,
                ProjectRemovalResultEvent.ProjectRemovalStatus.SUCCESS, message);

        publisher.publishEvent(removalResultEvent);
    }

    private void sendFailedResult(Long projectId, String message){
        ProjectRemovalResultEvent removalResultEvent = new ProjectRemovalResultEvent(this, projectId,
                ProjectRemovalResultEvent.ProjectRemovalStatus.FAIL, message);

        publisher.publishEvent(removalResultEvent);
    }














}
