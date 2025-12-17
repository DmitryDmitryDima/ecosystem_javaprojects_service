package com.ecosystem.projectsservice.javaprojects.processes.chains.project_creation_system_template;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.ConstructorSettingsForSystemTemplateBuild;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectStatus;
import com.ecosystem.projectsservice.javaprojects.processes.queue.UserEvent;
import com.ecosystem.projectsservice.javaprojects.processes.queue.UserEventContext;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import com.ecosystem.projectsservice.javaprojects.service.ProjectConstructor;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectLifecycleUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

@Service
public class ProjectInternalCreationEventChain {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private ProjectConstructor projectConstructor;


    @Autowired
    private ApplicationEventPublisher publisher;


    private static final String resultingEventName = "java_project_creation";

    @Async("taskExecutor")
    public void initChain(SecurityContext securityContext, RequestContext requestContext, ProjectBuildFromTemplateInfo info){

        // пользовательский контекст - мигрирует по всей цепочке и по итогу уходит в очередь
        UserEventContext eventContext = UserEventContext.builder()
                .correlationId(requestContext.getCorrelationId())
                .timestamp(Instant.now())
                .username(securityContext.getUsername())
                .userUUID(securityContext.getUuid())
                .build();

        // event data - будет пополняться по мере прохождения цепочки
        ProjectCreationEventData eventData = ProjectCreationEventData.builder()
                .name(info.getProjectName())
                .build();

        ProjectCreationUserPreference preference = ProjectCreationUserPreference.builder()
                .projectType(info.getProjectType())
                .needEntryPoint(info.isNeedEntryPoint())
                .build();





        // готовим ивент для дальнейшего шага. Все дальнейшие шаги имеют конкретную компенсацию
        ProjectCreationInitiationEvent projectCreationInitEvent = new ProjectCreationInitiationEvent(this);

        projectCreationInitEvent.setPaths(ProjectCreationPaths.builder()
                        .projectsPath(info.getProjectsPath())
                        .fileTemplatesPath(info.getFileTemplatesPath())
                        .instructionsPath(info.getInstructionsPath())
                .build());
        projectCreationInitEvent.setData(eventData);
        projectCreationInitEvent.setContext(eventContext);
        projectCreationInitEvent.setPreference(preference);

        System.out.println("initiation");
        publisher.publishEvent(projectCreationInitEvent);










    }



    @EventListener
    @Transactional
    public void prepareProjectEntityAndDatabaseLock(ProjectCreationInitiationEvent event){
        // готовим главную сущность
        Project project = new Project();
        project.setCreatedAt(Instant.now());
        project.setUserUUID(event.getContext().getUserUUID());
        project.setName(event.getData().getName());
        project.setStatus(ProjectStatus.CREATING); // статус creating защищает сущность от параллельных изменений
        project.setType(event.getPreference().getProjectType());

        try {
            project = projectRepository.saveAndFlush(project);
        }
        catch (Exception e){
            // тут может вылететь исключение нарушения constraint, если проект с атким именем уже существует
            // в данном случае компенсация не нужна, так как сущность записана не была
            if (e.getCause() instanceof DataIntegrityViolationException){

                sendFailedResult("java проект с таким именем уже существует", event.getContext(), event.getData());
            }
            else {

                sendFailedResult("Ошибка создания проекта. Причина: "+e.getMessage(), event.getContext(), event.getData());
            }
            return;
        }
        // сохрарняем для транзакций в следующих шагах
        event.getData().setProjectId(project.getId());



        // формируем ивент
        ProjectCreationProjectEntityCreatedEvent projectCreationProjectEntityCreatedEvent = new ProjectCreationProjectEntityCreatedEvent(this);

        projectCreationProjectEntityCreatedEvent.setPaths(event.getPaths());
        projectCreationProjectEntityCreatedEvent.setData(event.getData());
        projectCreationProjectEntityCreatedEvent.setContext(event.getContext());
        projectCreationProjectEntityCreatedEvent.setPreference(event.getPreference());

        System.out.println("project entity created");

        publisher.publishEvent(projectCreationProjectEntityCreatedEvent);

    }

    /*
    По логике у нас уже существует сущность project, однако мы всегда должны проводить проверку
     */


    @Transactional
    @EventListener
    public void prepareRootDirectory(ProjectCreationProjectEntityCreatedEvent event){
        Project project;

        try {
            Optional<Project> projectCheck = projectRepository.findById(event.getData().getProjectId());
            if (projectCheck.isEmpty()){
                throw new IllegalStateException("Сущность не была создана");
            }
            project = projectCheck.get();
        }
        catch (Exception e){

            sendFailedResult("Проблемы на сервисе, попробуйте позже. Причина: "+e.getMessage(), event.getContext(), event.getData());
            return;
        }

        // готовим сущность корневой папки - следующие шаги требуют компенсации



        Directory root = new Directory();

        root.setCreatedAt(Instant.now());
        root.setImmutable(true); // корневая папка строго иммутабельна
        root.setName(project.getName());
        root.setConstructedPath(Path.of(project.getName()).normalize().toString()); // todo путь будет строиться относительно корневой папки проекта

        System.out.println("before crash!");

        try {
            directoryRepository.save(root);
            project.setRoot(root);
            projectRepository.saveAndFlush(project);
            System.out.println(project.getRoot().getId());
        }
        catch (Exception e){
            e.printStackTrace();

            sendFailedResult("Неизвестная ошибка. Причина: "+e.getMessage(), event.getContext(), event.getData());

            compensation(project.getId(), Path.of(event.getPaths().getProjectsPath(), project.getName()).toString());
            return;

        }

        // создаем директорию на диске
        try {

            ProjectLifecycleUtils.createDirectory(Path.of(event.getPaths().getProjectsPath(), project.getName()));
        }
        catch (Exception e){

            sendFailedResult("Ошибка записи в диск: "+e.getMessage(), event.getContext(), event.getData());

            compensation(project.getId(), Path.of(event.getPaths().getProjectsPath(), project.getName()).toString());

            return;

        }

        System.out.println("root created");

        // формируем следующий ивент
        ProjectCreationRootWrittenEvent rootWrittenEvent = new ProjectCreationRootWrittenEvent(this );
        rootWrittenEvent.setContext(event.getContext());
        rootWrittenEvent.setData(event.getData());
        rootWrittenEvent.setPaths(event.getPaths());
        rootWrittenEvent.setPreference(event.getPreference());

        publisher.publishEvent(rootWrittenEvent);


    }



    @Transactional
    @EventListener
    public void createProjectStructure(ProjectCreationRootWrittenEvent rootWrittenEvent){

        Project project;

        try {
            Optional<Project> projectCheck = projectRepository.findById(rootWrittenEvent.getData().getProjectId());
            if (projectCheck.isEmpty()){
                throw new IllegalStateException("Сущность не была создана");
            }
            project = projectCheck.get();
            System.out.println(project.getRoot());
        }
        catch (Exception e){

            sendFailedResult("Проблемы на сервисе, попробуйте позже. Причина: "+e.getMessage(), rootWrittenEvent.getContext(),
                    rootWrittenEvent.getData());

            // все равно пытаемся удалить сущность на случай, если она была записана в базу, доступ к которой был потерян (todo retry)
            compensation(rootWrittenEvent.getData().getProjectId(),
                    Path.of(rootWrittenEvent.getPaths().getProjectsPath(), rootWrittenEvent.getData().getName()).toString());
            return;
        }


        // формируем настройки для конструктора. тут мы намеренно используем единый контекст из-за обилия транзакций
        ConstructorSettingsForSystemTemplateBuild settings = ConstructorSettingsForSystemTemplateBuild.builder()
                .projectType(rootWrittenEvent.getPreference().getProjectType())
                .project(project)
                .fileTemplatesPath(rootWrittenEvent.getPaths().getFileTemplatesPath())
                .instructionsPath(rootWrittenEvent.getPaths().getInstructionsPath())
                .projectsPath(rootWrittenEvent.getPaths().getProjectsPath())
                .needEntryPoint(rootWrittenEvent.getPreference().isNeedEntryPoint())
                .build();

        try {
            projectConstructor.buildProjectFromSystemTemplate(settings);


            // отпускаем флаг todo подумать над отдельным шагом
            project.setStatus(ProjectStatus.AVAILABLE);
            projectRepository.save(project);

            System.out.println("project structure created");



            // успешное завершение
            sendSuccessResult("Проект "+project.getName()+" создан!", rootWrittenEvent.getContext(), rootWrittenEvent.getData());
        }
        catch (Exception e){
            sendFailedResult("Ошибка при создании структуры проекта. Причина: "+e.getMessage(), rootWrittenEvent.getContext(), rootWrittenEvent.getData());
            compensation(project.getId(), Path.of(rootWrittenEvent.getPaths().getProjectsPath(), project.getName()).toString());
        }





    }



    // простая компенсация-удаляем директорию и бд
    @Transactional
    private void compensation(Long projectId, String projectPath){

        try {
            Optional<Project> project = projectRepository.findById(projectId);
            project.ifPresent((entity)->{
                projectRepository.delete(entity);
            });

            FileSystemUtils.deleteRecursively(Path.of(projectPath));
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }




    private void sendResult(String message, UserEventContext context, ProjectCreationEventData data){
        try {
            // todo симуляция задержки
            Thread.sleep(5000);
            UserEvent userEvent = UserEvent.builder()
                    .eventData(data)
                    .event_type(resultingEventName)
                    .context(context)
                    .message(message)
                    .build();
            publisher.publishEvent(userEvent);
        }
        catch (Exception e){
            // ошибка тут должна компенсироваться в будущем
            e.printStackTrace();
        }
    }

    private void sendFailedResult(String message, UserEventContext context, ProjectCreationEventData data){
        data.setStatus(ProjectCreationStatus.FAIL);
        sendResult(message, context, data);
    }

    private void sendSuccessResult(String message, UserEventContext context, ProjectCreationEventData data){
        data.setStatus(ProjectCreationStatus.SUCCESS);
        sendResult(message, context, data);
    }






}
