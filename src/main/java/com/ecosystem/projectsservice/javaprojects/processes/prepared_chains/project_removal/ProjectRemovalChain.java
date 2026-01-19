package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.project_removal;


import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectStatus;
import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.OutboxDeclarativeChain;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.UserPersonalEvent;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Path;
import java.util.Optional;

// todo добавить инвалидацию кеша
@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_REMOVAL)
public class ProjectRemovalChain extends OutboxDeclarativeChain<ProjectRemovalEvent> {


    @Autowired
    private DirectoryRepository directoryRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private FileRepository fileRepository;

    @Override
    public void configure() throws Exception {

    }

    @Override
    @Async("taskExecutor")
    @EventListener
    public void catchEvent(ProjectRemovalEvent event) {
        super.processEvent(event);
    }

    @Override
    public void compensationStrategy(ProjectRemovalEvent event) {

    }

    @Override
    protected ExternalEvent<? extends ExternalEventContext> bindResultingEvent() {
        return new UserPersonalEvent();
    }


    @OpeningStep(name = "blockProject")
    @Next(name = "clearDisk")
    @Message
    public ProjectRemovalEvent blockProject(ProjectRemovalEvent event){

        event.setMessage("блокируем проект");

        String fullPath = transaction().execute(status -> {

            // pessimistic write
            Optional<Project> existenceCheck = projectRepository.findByIdForUpdate(event.getExternalData().getProjectId());

            if (existenceCheck.isEmpty()){

                throw new IllegalStateException("проект не найден");

            }
            Project project = existenceCheck.get();


            if (!project.getUserUUID().equals(event.getContext().getUserUUID())){

                throw new IllegalStateException("ошибка доступа");
            }

            if (project.getStatus()!= ProjectStatus.AVAILABLE){


               throw new IllegalStateException("ошибка статуса проекта. Возможно. он запущен?");
            }

            // блокировка специальным статусом
            project.setStatus(ProjectStatus.REMOVING);

            // формируем полный путь до проекта

            return Path.of(event.getInternalData().getProjectPath(), project.getName()).toString();

        });

        event.getInternalData().setProjectPath(fullPath);



        return event;
    }

    @Step(name = "clearDisk")
    @Message
    @Next(name = "dbRemove")
    public ProjectRemovalEvent clearDisk(ProjectRemovalEvent event) throws Exception{

        event.setMessage("чистим диск");

        FileSystemUtils.deleteRecursively(Path.of(event.getInternalData().getProjectPath()));


        return event;
    }

    @EndingStep(name = "dbRemove")
    public ProjectRemovalEvent dbRemove(ProjectRemovalEvent event){



        transaction().execute(status -> {

            Optional<Project> projectCheck = projectRepository.findById(event.getExternalData().getProjectId());
            if (projectCheck.isEmpty()){

                throw new IllegalStateException("Ошибка удаления. Не найден id проекта");
            }

            projectRepository.delete(projectCheck.get());


           return null;
        });

        return event;
    }







}
