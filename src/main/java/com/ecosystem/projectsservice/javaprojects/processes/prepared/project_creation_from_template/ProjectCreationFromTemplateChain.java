package com.ecosystem.projectsservice.javaprojects.processes.prepared.project_creation_from_template;


import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.ConstructorSettingsForSystemTemplateBuild;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectStatus;
import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventName;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.OutboxDeclarativeChain;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.markers.UserEvent;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.ProjectConstructor;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectLifecycleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

@Service
@ExternalResultName(event = ExternalEventName.JAVA_PROJECT_CREATION_FROM_TEMPLATE)
public class ProjectCreationFromTemplateChain extends OutboxDeclarativeChain<ProjectCreationFromTemplateEvent> {


    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private ProjectConstructor projectConstructor;


    @Override
    public void configure() throws Exception {

    }

    @Override
    @Async("taskExecutor")
    @EventListener
    public void catchEvent(ProjectCreationFromTemplateEvent event) {
        super.processEvent(event);
    }

    @Override
    public void compensationStrategy(ProjectCreationFromTemplateEvent event) {
        String step = event.getInternalData().getCurrentStep();
        if (!step.equals("projectEntityCreation")){

        }
    }

    @Override
    protected ExternalEvent<? extends ExternalEventContext> bindResultingEvent() {
        return new UserEvent();
    }

    @OpeningStep(name = "projectEntityCreation")
    @Message
    @Next(name = "directoryCreation")
    public ProjectCreationFromTemplateEvent projectEntityCreation(ProjectCreationFromTemplateEvent event){

        event.setMessage("Создаем сущность проекта");



        // создаем новую сущность с заданными параметрами

        Project project = new Project();
        project.setCreatedAt(Instant.now());
        project.setUserUUID(event.getContext().getUserUUID());
        project.setName(event.getExternalData().getName());
        project.setStatus(ProjectStatus.CREATING); // статус creating защищает сущность от параллельных изменений
        project.setType(event.getInternalData().getProjectType());

        Long id = transaction().execute(status -> projectRepository.saveAndFlush(project).getId());

        event.getExternalData().setProjectId(id);


        return event;
    }

    @Step(name="directoryCreation")
    @Message
    @Next(name="prepareStructure")
    public ProjectCreationFromTemplateEvent directoryCreation(ProjectCreationFromTemplateEvent event) throws Exception{


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

            /*
            путь в базе данных строится относительно папки проекта, все остальное конструируется исходя из запроса
            */
            root.setConstructedPath(project.getName());

            directoryRepository.save(root);


            project.setRoot(root);

            return null;
        });

        // пишем в диск

        ProjectLifecycleUtils.createDirectory(Path.of(event.getInternalData().getProjectsPath(),  event.getExternalData().getName()));




        return event;
    }



    @EndingStep(name="prepareStructure")
    public ProjectCreationFromTemplateEvent prepareStructure(ProjectCreationFromTemplateEvent event) throws Exception{
        event.setMessage("Готовим первоначальную структуру проекта");

        transaction().execute(status -> {

            Project project = projectRepository.findById(event.getExternalData().getProjectId()).orElseThrow(()->
                    new IllegalStateException("Сущность не была создана"));

            // формируем настройки для конструктора. тут мы намеренно используем единый контекст из-за обилия транзакций
            ConstructorSettingsForSystemTemplateBuild settings = ConstructorSettingsForSystemTemplateBuild.builder()
                    .projectType(event.getInternalData().getProjectType())
                    .project(project)
                    .fileTemplatesPath(event.getInternalData().getFileTemplatesPath())
                    .instructionsPath(event.getInternalData().getInstructionsPath())
                    .projectsPath(event.getInternalData().getProjectsPath())
                    .needEntryPoint(event.getInternalData().isNeedEntryPoint())
                    .build();

            try {
                projectConstructor.buildProjectFromSystemTemplate(settings);
            } catch (Exception e) {
                throw new RuntimeException("Ошибка создания проекта: "+e.getCause().getMessage());
            }



            project.setStatus(ProjectStatus.AVAILABLE);

            return null;
        });


        return event;
    }




}
