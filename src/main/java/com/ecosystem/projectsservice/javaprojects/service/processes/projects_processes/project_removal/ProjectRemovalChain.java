package com.ecosystem.projectsservice.javaprojects.service.processes.projects_processes.project_removal;



import com.ecosystem.projectsservice.javaprojects.dto.projects.state.updates.CachedFilesInvalidation;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.updates.ProjectStructureInvalidation;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.ProjectParticipant;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectPrivacyLevel;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.read.SnapshotService;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.update.HotLayerUpdater;
import com.ecosystem.projectsservice.javaprojects.service.storage.UserContentStorage;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.routing_strategies.NotificationStrategy;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;



// TODO инвалидация кешей


@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_REMOVAL)
public class ProjectRemovalChain extends ControlledOutboxChain<ProjectRemovalEvent> {



    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectRemovalCompensator compensator;

    @Autowired
    private UserContentStorage storageService;


    @Autowired
    private HotLayerUpdater hotLayer;



    @Autowired
    private SnapshotService snapshotService;


    @Override
    public void configure() throws Exception {

    }

    @Override
    @Async("chainExecutor")
    @EventListener
    public void catchEvent(ProjectRemovalEvent event) {
        super.processEvent(event);
    }

    @Override
    public void compensationStrategy(ProjectRemovalEvent event) {
        compensator.compensation(event);
    }

    @Override
    protected ExternalEvent<? extends ExternalEventContext> bindResultingEvent() {
        return new ProjectEventFromUser();
    }

    @Override
    protected void setProcessAssociations(ProjectRemovalEvent event) {

    }


    @OpeningStep(name = "blockProject")
    @Next(name = "clearStorage")
    @Message
    public void blockProject(ProjectRemovalEvent event){

        event.setMessage("блокируем проект");


        // проверяем права и блокируем проект
        Project entity = transaction().execute(status -> {

            // pessimistic write
            Optional<Project> existenceCheck = projectRepository.findByIdForUpdate(event.getExternalData().getProjectId());

            if (existenceCheck.isEmpty()){

                throw new IllegalStateException("проект не найден");

            }
            Project project = existenceCheck.get();


            if (!project.getUserUUID().equals(event.getContext().getUserUUID())){

                throw new IllegalStateException("ошибка доступа. Вы не можете удалить этот проект");
            }

            if (project.getStatus()!= ProjectStatus.AVAILABLE){


                throw new IllegalStateException("ошибка статуса проекта. Возможно. он запущен?");
            }

            // блокировка специальным статусом
            project.setStatus(ProjectStatus.REMOVING);

            return project;

        });

        // модифицируем стратегию рассылки

        // вставляем необходимые дополнения в контекст
        // событие удаления проекта рассылается персонально участникам проекта и его автору, либо открытый, либо закрытый канал
        NotificationStrategy notificationStrategy = new NotificationStrategy();
        List<UUID> toNotify = new ArrayList<>();
        toNotify.add(entity.getUserUUID());
        toNotify.addAll(entity.getParticipants().stream().map(ProjectParticipant::getUserUUID).toList());

        if (entity.getPrivacyLevel() == ProjectPrivacyLevel.OPEN) {
            notificationStrategy.setPublicChannel(toNotify);
        } else {
            notificationStrategy.setPrivateChannel(toNotify);
        }


        event.getContext().setNotificationStrategy(notificationStrategy);



        // инвалидируем структуру проекта в кеше
        try {
            hotLayer.projectStructureInvalidation(
                    new ProjectStructureInvalidation(event.getContext().getProjectId())
            );
        }

        catch (Exception e){

        }







    }



    /*
    для удаления файлов в хранилище сначала нужно извлечь список всех сопряженных с проектом файлов
     */
    @Step(name = "clearStorage")
    @Message
    @MaxRetry(maxCount = 2)
    @Next(name = "dbRemove")
    public void clearStorage(ProjectRemovalEvent event) throws Exception{


        event.setMessage("чистим хранилище");

        List<UUID> files = transaction().execute(status -> {

            Optional<Project> existenceCheck = projectRepository
                    .findById(event.getExternalData().getProjectId());


            if (existenceCheck.isEmpty()) throw new IllegalStateException("несогласованность процесса" +
                    " - сущность проекта не найдена");

            Project project = existenceCheck.get();

            Directory root = project.getRoot();

            if (root == null){
                throw new IllegalStateException("несогласованность процесса " +
                        "- отсутствует корневая директория");
            }



            return snapshotService.getAllFilesBelowDirectory(root.getId())
                    .stream().map(FileReadOnly::getId).toList();







        });



        storageService.deleteBatch(files.stream().map(UUID::toString).toList());

        // инвалидируем кеш
        try {
            hotLayer.filesInvalidation(new CachedFilesInvalidation(files));

        }
        catch (Exception e){

        }








    }

    @EndingStep(name = "dbRemove")
    public void dbRemove(ProjectRemovalEvent event){



        transaction().execute(status -> {

            Optional<Project> projectCheck = projectRepository.findById(event.getExternalData().getProjectId());
            if (projectCheck.isEmpty()){

                throw new IllegalStateException("Несогласованность процесса - отсутствует сущность проекта");
            }

            projectRepository.delete(projectCheck.get());


           return null;
        });


    }







}
