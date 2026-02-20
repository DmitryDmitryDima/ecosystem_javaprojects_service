package com.ecosystem.projectsservice.javaprojects.service.projects;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.ProjectSnapshot;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.SimpleFileInfo;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.FileSaveRequest;
import com.ecosystem.projectsservice.javaprojects.model.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.ProjectParticipant;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectPrivacyLevel;
import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.broadcastable_action.BroadcastableAction;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ProjectEventFromUserContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.FileRemovalExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_removal.FileRemovalChain;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_removal.FileRemovalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_removal.FileRemovalInternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.filesave.FileSaveChain;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.filesave.FileSaveEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.filesave.FileSaveInternalData;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.TriggerAnswer;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.TriggersAggregator;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryJDBCRepository;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectRepository;
import com.ecosystem.projectsservice.javaprojects.service.cache.FileContentCache;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectActionsUtils;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;


// todo методы проверки могут быть оптимизированы кастомный join requests

// ответственность внешнего сервиса - проверка прав. ответственность асинхронных внутренних цепочек - внутренние операции с бд, диском и кешем
// те данные, что могут быть сконструированы исходя из проверки, вставляем сразу

@Service
public class ProjectActionsService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ProjectActionsUtils utils;

    @Autowired
    private TriggersAggregator triggersAggregator;



    @Autowired
    private DirectoryJDBCRepository directoryJDBCRepository;



    @Autowired
    private FileContentCache<FileDTO, Long> fileContentCache;


    // сервис для генерации действий из одного шага с публикацией внешнего ивента
    @Autowired
    private BroadcastableAction broadcast;


    // процессы
    @Autowired
    private FileSaveChain fileSaveChain;

    @Autowired
    private FileRemovalChain fileRemovalChain;








    @Value("${storage.system}")
    private String systemStoragePath;

    @Value("${storage.user}")
    private String userStoragePath;



    public void triggerPollingProcess(SecurityContext securityContext,
                                      RequestContext requestContext, Long projectId, TriggerAnswer answer) throws Exception {

        // проверяем, имеет ли право отвечающий на то, чтобы взаимодействовать с процессом, связанным с проектом
        // пример кейса - участника выкинули, но у него еще есть process uuid
        checks(securityContext, requestContext, projectId);

        // обогащаем контекстом
        answer.setUser(securityContext.getUuid());
        answer.setCorrelationId(requestContext.getCorrelationId());
        answer.setRenderId(requestContext.getRenderId());

        triggersAggregator.feedTrigger(answer);



    }




    // метод проверки принадлежности файла к проекту и прав на взаимодействие с ним
    private FileReadOnly checkFileRightsAndGetItFromProject(Project project, Long fileId){
        ProjectSnapshot snapshot = getProjectSnapshot(project.getRoot().getId());

        FileReadOnly dbFile =null;

        for (FileReadOnly fileReadOnly:snapshot.getFiles()){

            if (fileReadOnly.getId().equals(fileId)){
                if (fileReadOnly.isHidden() || !fileReadOnly.getStatus().equals(FileStatus.AVAILABLE)){

                    throw new IllegalStateException("Файл не доступен");
                }
                dbFile = fileReadOnly;
                return dbFile;

            }
        }

        throw new IllegalStateException("Файл отсутствует или не принадлежит проекту");


    }




    // данный метод ориентируется на выброс исключений, перехватываемых в advice
    @Transactional
    public ProjectDTO readProject(SecurityContext securityContext, RequestContext requestContext, Long projectId) throws Exception{



        Project project = checks(securityContext, requestContext, projectId);

        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setProjectType(project.getType());
        projectDTO.setStatus(project.getStatus());
        projectDTO.setName(project.getName());
        projectDTO.setAuthor(project.getUserUUID());

        utils.generateStructureForDTO(project.getRoot().getId(), projectDTO, getProjectSnapshot(project.getRoot().getId()));

        return projectDTO;
    }






    // todo механизм автосохранения не полагается на цепочку, так как работает только с redis
    @Transactional
    public void autosave(SecurityContext securityContext,
                         RequestContext requestContext,
                         Long projectId,
                         Long fileId,
                         FileSaveRequest request) throws Exception{



        Project project = checks(securityContext, requestContext, projectId);

        // безопасно читаем файл из бд - при статусе available его можно писать в кеш
        FileReadOnly dbFile = checkFileRightsAndGetItFromProject(project, fileId);




        FileDTO fileDTO = FileDTO.builder()
                .content(request.getContent())
                .constructedPath(dbFile.getConstructed_path())
                .id(dbFile.getId())
                .extension(dbFile.getExtension())
                .name(dbFile.getName())
                .projectId(projectId)
                .ownerUUID(project.getUserUUID())
                        .build();




        broadcast.statelessAction(
                ()-> fileContentCache.save(fileId, fileDTO))
                .withContext(()->ProjectEventFromUserContext.from(securityContext, requestContext, projectId, List.of()))
                .withData(()->{
                    FileSaveExternalData externalData = new FileSaveExternalData();
                    externalData.setContent(request.getContent());
                    externalData.setFileId(fileId);
                    return externalData;})
                .withEvent(ProjectEventFromUser::new)
                .withType(ExternalEventType.JAVA_PROJECT_FILE_SAVE).withMessage("Файл сохранен")
                .execute();


    }


    @Transactional
    public void removeFile(SecurityContext securityContext,
                           RequestContext requestContext,
                           Long projectId,
                           Long fileId) throws Exception{

        System.out.println(requestContext.getCorrelationId());

        Project project = checks(securityContext, requestContext, projectId);

        if (project.getEntryPoint().getId().equals(fileId)){
            throw new IllegalStateException("файл входит в выбранный конфиг запуска");
        }

        FileReadOnly dbFile = checkFileRightsAndGetItFromProject(project, fileId);

        if (dbFile.isImmutable()){
            throw new IllegalStateException("файл нельзя удалить");
        }


        FileRemovalEvent mainEvent = new FileRemovalEvent();
        mainEvent.setMessage("Удаляем файл");
        ProjectEventFromUserContext context = ProjectEventFromUserContext.from(securityContext, requestContext, projectId, List.of());

        mainEvent.setContext(context);

        FileRemovalInternalData internalData = new FileRemovalInternalData();
        internalData.setFilePath(Path.of(userStoragePath,
                project.getUserUUID().toString(),
                "projects", dbFile.getConstructed_path()).normalize().toString());


        mainEvent.setInternalData(internalData);

        FileRemovalExternalData externalData = new FileRemovalExternalData();


        externalData.setFileId(fileId);
        externalData.setExtension(dbFile.getExtension());
        // не путать с uuid того, кто выполняет запрос - это могут быть разные люди
        externalData.setFileOwner(project.getUserUUID());
        externalData.setName(dbFile.getName());



        mainEvent.setExternalData(externalData);


        fileRemovalChain.init(mainEvent);






    }

    // метод форсированной записи файла в диск - гарантирует согласованность данных во всех связанных с файлом слоях - диск, бд, кеш
    // используем outbox цепочку - операция сложная, затрагивает несколько систем сразу
    @Transactional
    public void saveFile(SecurityContext securityContext,
                         RequestContext requestContext,
                         Long projectId,
                         Long fileId,
                         FileSaveRequest request) throws Exception {

        Project project = checks(securityContext, requestContext, projectId);

        FileReadOnly dbFile = checkFileRightsAndGetItFromProject(project, fileId);


        FileSaveEvent mainEvent = new FileSaveEvent();
        mainEvent.setMessage("Сохраняем файл...");
        ProjectEventFromUserContext context = ProjectEventFromUserContext.from(securityContext, requestContext, projectId, List.of());

        mainEvent.setContext(context);

        FileSaveInternalData internalData = new FileSaveInternalData();
        internalData.setFilePath(Path.of(userStoragePath,
                securityContext.getUuid().toString(),
                "projects", dbFile.getConstructed_path()).normalize().toString());
        mainEvent.setInternalData(internalData);

        FileSaveExternalData externalData = new FileSaveExternalData();
        externalData.setContent(request.getContent());
        externalData.setFileId(fileId);
        externalData.setExtension(dbFile.getExtension());
        // не путать с uuid того, кто выполняет запрос - это могут быть разные люди
        externalData.setFileOwner(project.getUserUUID());
        externalData.setName(dbFile.getName());
        externalData.setExtension(dbFile.getExtension());

        mainEvent.setExternalData(externalData);

        fileSaveChain.init(mainEvent);









    }



    // todo внутри данной функции должен быть добавлен запрос участников проекта вместе с ролями (admin, author, viewer)
    private ProjectSnapshot getProjectSnapshot(Long rootId){
        // извлекаем все папки, принадлежащие проекту, вместе с зависимостями
        List<DirectoryReadOnly> directories = directoryJDBCRepository.loadAWholeStructureFromRoot(rootId);
        // извлекаем все файлы, принадлежащие проекту
        List<FileReadOnly> files = directoryJDBCRepository.loadFilesAssosiatedWithDirectories(
                directories.stream().map(DirectoryReadOnly::getId).toList()
        );
        return ProjectSnapshot.builder()
                .directories(directories)
                .files(files)
                .build();
    }

    @Transactional
    public List<SimpleFileInfo> getRecentFiles(SecurityContext securityContext, RequestContext requestContext, Long projectId) throws Exception {
        Project project = checks(securityContext, requestContext, projectId);

        ProjectSnapshot snapshot = getProjectSnapshot(project.getRoot().getId());

        return utils.getRecentFiles(snapshot);



    }

    /*
    todo - вопрос - создает ли чтение с диска запись в кеш?
     */
    @Transactional
    public FileDTO readFile(SecurityContext securityContext, RequestContext requestContext, Long projectId, Long fileId) throws Exception{
        Project project = checks(securityContext, requestContext, projectId);

        Optional<FileDTO> fileDTOFromCache = fileContentCache.read(fileId);

        if (fileDTOFromCache.isEmpty()){

            FileReadOnly dbFile = checkFileRightsAndGetItFromProject(project, fileId);

            FileDTO fileDTO = FileDTO.builder()
                    .name(dbFile.getName())
                    .extension(dbFile.getExtension())
                    .constructedPath(dbFile.getConstructed_path())
                    .id(dbFile.getId())
                    .ownerUUID(project.getUserUUID())
                    .projectId(projectId)
                    .build();

            try {
                Path path = ProjectUtils.constructPathToFile(userStoragePath, project, dbFile.getConstructed_path());
                fileDTO.setContent(ProjectUtils.readFile(path));

            }
            catch (Exception e){
                throw new IllegalStateException("Ошибка чтения файла. Причина: "+e.getMessage());
            }

            return fileDTO;






        }

        else return fileDTOFromCache.get();


    }





    // так как каждый запрос базируется на id, мы должны извлечь проект и провести базовую проверку по нему

    // todo это можно кешировать с помощью ограниченного токена
    // todo тут же извлекается лист участников проекта
    private Project checks(SecurityContext securityContext, RequestContext requestContext, Long projectId) throws Exception{
        Optional<Project> projectCheck = projectRepository.findById(projectId);

        if (projectCheck.isEmpty()) throw new IllegalStateException("Проекта не существует");

        Project project = projectCheck.get();

        // действие выполняется хозяином проекта
        if (project.getUserUUID().equals(securityContext.getUuid())) return project;


        boolean existed = false;
        List<ProjectParticipant> participants = project.getParticipants(); // извлекаем один раз для lazy транзакции
        for (ProjectParticipant participant:participants){
            if (participant.getUserUUID().equals(securityContext.getUuid())){
                existed = true;
                break;
            }
        }
        if (!existed){
            throw new IllegalStateException("Пользователь не является участником проекта");

        }



        // todo проверка доступа к проекту

        return project;
    }


}
