package com.ecosystem.projectsservice.javaprojects.service.processes.projects_processes.project_creation_from_template;


import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.constructors.BuildProperties;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.constructors.FileTemplateEnvelope;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectStatus;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.constructors.ProjectYamlConstructor;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.UserPersonalEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_CREATION_FROM_SYSTEM_INSTRUCTION)
public class ProjectCreationFromSystemInstructionChain extends ControlledOutboxChain<
        ProjectCreationFromSystemInstructionEvent
        > {



    @Autowired
    private ProjectRepository projectRepository;


    @Autowired
    private DirectoryRepository directoryRepository;


    @Autowired
    private ProjectYamlConstructor constructor;

    @Override
    protected ExternalEvent<? extends ExternalEventContext> bindResultingEvent() {
        return new UserPersonalEvent();
    }

    @Override
    protected void setProcessAssociations(ProjectCreationFromSystemInstructionEvent event) {

    }

    @Override
    @Async("chainExecutor")
    @EventListener
    public void catchEvent(ProjectCreationFromSystemInstructionEvent event) {
        super.processEvent(event);
    }



    @OpeningStep(name = "project_entity_creation")
    @Next(name = "root_creation")
    @Message
    public void createProjectEntity(ProjectCreationFromSystemInstructionEvent event){
        Project project = new Project();
        project.setCreatedAt(Instant.now());
        project.setUserUUID(event.getContext().getUserUUID());
        project.setName(event.getExternalData().getName());
        project.setStatus(ProjectStatus.CREATING); // статус creating защищает сущность от параллельных изменений
        project.setType(event.getInternalData().getProjectType());
        project.setPrivacyLevel(event.getInternalData().getPrivacyLevel());

        UUID id = transaction().execute(status -> projectRepository.saveAndFlush(project).getId());

        event.getExternalData().setProjectId(id);
    }

    @Step(name = "root_creation")
    @Next(name = "structure_creation")
    @Message
    public void createRoot(ProjectCreationFromSystemInstructionEvent event){
        event.setMessage("Создаем директорию");

        transaction().execute(status -> {

            Optional<Project> projectCheck = projectRepository.findById(event.getExternalData().getProjectId());
            if (projectCheck.isEmpty()){
                throw new IllegalStateException("Сущность не была создана");
            }
            Project project = projectCheck.get();

            Directory root = new Directory();

            root.setCreatedAt(Instant.now());
            root.setImmutable(true); // корневая папка строго иммутабельна
            root.setName(project.getName());
            root.setHidden(true);

            /*
            путь в базе данных строится относительно папки проекта, все остальное конструируется исходя из запроса
            */
            root.setConstructedPath(project.getName());

            directoryRepository.save(root);


            project.setRoot(root);

            return null;
        });
    }


    @Step(name = "structure_creation")
    @Next(name = "project_release")
    @MaxDuration(time = 10) // default sec
    public void createStructure(ProjectCreationFromSystemInstructionEvent event){


        event.setMessage("Готовим первоначальную структуру проекта");



        // готовим бд структуру и создаем экстракт файлов с uuid для объектного хранилища

        List<FileTemplateEnvelope> files = transaction().execute(status -> {

            Project project = projectRepository
                    .findById(event.getExternalData().getProjectId()).orElseThrow(()->
                    new IllegalStateException("Сущность не была создана"));


            BuildProperties buildProperties = BuildProperties.builder()
                    .projectType(event.getInternalData().getProjectType())
                    .needEntryPoint(event.getInternalData().isNeedEntryPoint())
                    .project(project)
                    .build();

            return constructor.buildDatabaseStructureAndPrepareFileTemplates(buildProperties);

        });


        constructor.createStorageObjects(files);








    }

    @EndingStep(name = "project_release")
    public void releaseProject(ProjectCreationFromSystemInstructionEvent event){
        event.setMessage("выпускаем проект в систему");


        transaction().execute(status -> {

            Project project = projectRepository
                    .findById(event.getExternalData().getProjectId()).orElseThrow(()->
                            new IllegalStateException("Сущность не была создана"));


            project.setStatus(ProjectStatus.AVAILABLE);

            return null;
        });


    }



    /* TODO
    очистка должна быть более комплексной - проход по сщуности в поисках файлов
     - их удаление по id в хранилище, если они есть

     Фоновый воркер должен периодически проверять согласованность между хранилищем и бд, пользуясь общим uuid
     и выявляя сущности "сироты"
     */
    @Override
    public void compensationStrategy(ProjectCreationFromSystemInstructionEvent event) {
        String step = event.getInternalData().getCurrentStep();
        if (!step.equals("project_entity_creation")){
            // очистка сущности
            transaction().execute(status -> {

                projectRepository.deleteById(event.getExternalData().getProjectId());

                return null;
            });

        }
    }
}
