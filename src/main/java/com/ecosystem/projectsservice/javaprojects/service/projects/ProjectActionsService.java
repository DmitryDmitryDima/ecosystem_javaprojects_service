package com.ecosystem.projectsservice.javaprojects.service.projects;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.*;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.DirectoryAddRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.FileAddRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.FileSaveRequest;
import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.ProjectParticipant;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.broadcastable_action.BroadcastableAction;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ProjectEventFromUserContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.DirectoryAddExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.FileAddExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.FileRemovalExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.directory_add.DirectoryAddChain;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.directory_add.DirectoryAddEvent;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.directory_add.DirectoryAddInternalData;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_add.FileAddChain;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_add.FileAddEvent;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_add.FileAddInternalData;
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
import java.util.UUID;


// todo методы проверки могут быть оптимизированы кастомный join requests

// ответственность внешнего сервиса - проверка прав. ответственность асинхронных внутренних цепочек - внутренние операции с бд, диском и кешем
// те данные, что могут быть сконструированы исходя из проверки, вставляем сразу

@Service
public class ProjectActionsService {







    @Autowired
    private ProjectActionsUtils utils;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private ProjectAccessValidator accessValidator;

    @Autowired
    private TriggersAggregator triggersAggregator;






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

    @Autowired
    private FileAddChain fileAddChain;

    @Autowired
    private DirectoryAddChain directoryAddChain;








    @Value("${storage.system}")
    private String systemStoragePath;

    @Value("${storage.user}")
    private String userStoragePath;



    public void triggerPollingProcess(SecurityContext securityContext,
                                      RequestContext requestContext, UUID projectId, TriggerAnswer answer) throws Exception {

        // проверяем, имеет ли право отвечающий на то, чтобы взаимодействовать с процессом, связанным с проектом
        // пример кейса - участника выкинули, но у него еще есть process uuid
        accessValidator.validateAccess(securityContext, requestContext, projectId);

        // обогащаем контекстом
        answer.setUser(securityContext.getUuid());
        answer.setCorrelationId(requestContext.getCorrelationId());
        answer.setRenderId(requestContext.getRenderId());

        triggersAggregator.feedTrigger(answer);



    }









    // данный метод ориентируется на выброс исключений, перехватываемых в advice
    @Transactional
    public ProjectDTO readProject(SecurityContext securityContext, RequestContext requestContext, UUID projectId) throws Exception{



        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);

        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setProjectType(project.getType());
        projectDTO.setStatus(project.getStatus());
        projectDTO.setName(project.getName());
        projectDTO.setAuthor(project.getUserUUID());
        projectDTO.setParticipants(project.getParticipants().stream().map(ProjectParticipant::getUserUUID).toList());

        utils.generateStructureForDTO(project.getRoot().getId(), projectDTO, snapshotService.getSnapshot(project.getRoot().getId()));

        return projectDTO;
    }






    // todo механизм автосохранения не полагается на цепочку, так как работает только с redis
    @Transactional
    public void autosave(SecurityContext securityContext,
                         RequestContext requestContext,
                         UUID projectId,
                         Long fileId,
                         FileSaveRequest request) throws Exception{



        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);

        // безопасно читаем файл из снимка бд - при статусе available его можно писать в кеш


        StructureSnapshot snapshot = snapshotService.getSnapshot(project.getRoot().getId());

        // извлекаем файл из снимка, если он есть
        Optional<FileReadOnly> check = utils.findAvailableFile(snapshot, fileId);



        if (check.isEmpty()){
            throw new IllegalStateException("Файл отсутствует, недоступен или не принадлежит проекту");
        }

        FileReadOnly dbFile = check.get();

        // готовим dto. при операции чтения это dto будет прочтено из файлового кеша
        FileDTO fileDTO = FileDTO.builder()
                .content(request.getContent())
                .constructedPath(dbFile.getConstructed_path())
                .id(dbFile.getId())
                .extension(dbFile.getExtension())
                .name(dbFile.getName())
                .projectId(projectId)
                .ownerUUID(project.getUserUUID())
                        .build();



        // ивент пересылается только подписчикам проекта
        broadcast.statelessAction(
                ()-> fileContentCache.save(fileId, fileDTO))
                .withContext(()->ProjectEventFromUserContext.from(securityContext, requestContext, project, null, null))
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
    public void addDirectory(SecurityContext securityContext,
                             RequestContext requestContext,
                             UUID projectId,
                             DirectoryAddRequest directoryAddRequest) throws Exception{

        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);

        DirectoryAddEvent event = new DirectoryAddEvent();

        ProjectEventFromUserContext context = ProjectEventFromUserContext.from(securityContext, requestContext, project,
                null,
                null);

        DirectoryAddExternalData externalData = new DirectoryAddExternalData();
        externalData.setParentId(directoryAddRequest.getParentId());
        externalData.setName(directoryAddRequest.getName());

        DirectoryAddInternalData internalData = new DirectoryAddInternalData();
        internalData.setProjectRoot(project.getRoot().getId());
        internalData.setProjectsPath(Path.of(userStoragePath,
                project.getUserUUID().toString(),
                "projects").normalize().toString());

        event.setContext(context);
        event.setInternalData(internalData);
        event.setExternalData(externalData);
        event.setMessage("создаем директорию");

    }

    @Transactional
    public void addFile(SecurityContext securityContext,
                        RequestContext requestContext,
                        UUID projectId,
                        FileAddRequest fileAddRequest) throws Exception {


        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);

        FileAddEvent fileAddEvent = new FileAddEvent();


        ProjectEventFromUserContext context = ProjectEventFromUserContext.from(securityContext, requestContext, project,
                null,
                null);


        FileAddExternalData externalData = new FileAddExternalData();
        externalData.setFilename(fileAddRequest.getFilename());
        externalData.setExtension(fileAddRequest.getExtension());
        externalData.setParentId(fileAddRequest.getParentId());

        FileAddInternalData internalData = new FileAddInternalData();
        internalData.setProjectsPath(Path.of(userStoragePath,
                project.getUserUUID().toString(),
                "projects").normalize().toString());

        internalData.setProjectRoot(project.getRoot().getId());

        fileAddEvent.setContext(context);
        fileAddEvent.setExternalData(externalData);
        fileAddEvent.setInternalData(internalData);
        fileAddEvent.setMessage("Создание файла "+fileAddRequest.getFilename()+"."+fileAddRequest.getExtension());

        fileAddChain.init(fileAddEvent);

    }


    @Transactional
    public void removeFile(SecurityContext securityContext,
                           RequestContext requestContext,
                           UUID projectId,
                           Long fileId) throws Exception{

        System.out.println(requestContext.getCorrelationId());

        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);

        // быстрая проверка - если файл - часть конфигурации, нужно попросить пользователя его изменить
        if (project.getEntryPoint().getId().equals(fileId)){
            throw new IllegalStateException("файл входит в выбранный конфиг запуска");
        }




        FileRemovalEvent mainEvent = new FileRemovalEvent();
        mainEvent.setMessage("Удаляем файл");

        // ивент пересылается только подписчикам комнаты
        ProjectEventFromUserContext context = ProjectEventFromUserContext.from(securityContext,
                requestContext,
                project,
                null, null);


        mainEvent.setContext(context);

        FileRemovalInternalData internalData = new FileRemovalInternalData();


        internalData.setProjectRoot(project.getRoot().getId());
        internalData.setProjectsPath(Path.of(userStoragePath,
                project.getUserUUID().toString(),
                "projects").normalize().toString());




        mainEvent.setInternalData(internalData);

        FileRemovalExternalData externalData = new FileRemovalExternalData();


        externalData.setFileId(fileId);

        // не путать с uuid того, кто выполняет запрос - это могут быть разные люди
        externalData.setFileOwner(project.getUserUUID());

        mainEvent.setExternalData(externalData);

        fileRemovalChain.init(mainEvent);






    }

    // метод форсированной записи файла в диск - гарантирует согласованность данных во всех связанных с файлом слоях - диск, бд, кеш
    // используем outbox цепочку - операция сложная, затрагивает несколько систем сразу
    @Transactional
    public void saveFile(SecurityContext securityContext,
                         RequestContext requestContext,
                         UUID projectId,
                         Long fileId,
                         FileSaveRequest request) throws Exception {

        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);




        FileSaveEvent mainEvent = new FileSaveEvent();
        mainEvent.setMessage("Сохраняем файл...");

        // конструируем контект - комната проекта, ивент не требует дополнительных стратегий рассылки
        ProjectEventFromUserContext context = ProjectEventFromUserContext
                .from(securityContext, requestContext, project, null, null);

        mainEvent.setContext(context);

        // внутренние данные - необходима начальная папка проекта (для проверки принадлежности) и путь до проектов
        FileSaveInternalData internalData = new FileSaveInternalData();


        internalData.setProjectRoot(project.getRoot().getId());
        internalData.setProjectsPath(Path.of(userStoragePath,
                project.getUserUUID().toString(),
                "projects").normalize().toString());



        mainEvent.setInternalData(internalData);


        // внешние данные
        FileSaveExternalData externalData = new FileSaveExternalData();
        externalData.setContent(request.getContent());
        externalData.setFileId(fileId);

        // не путать с uuid того, кто выполняет запрос - это могут быть разные люди
        externalData.setFileOwner(project.getUserUUID());


        mainEvent.setExternalData(externalData);

        fileSaveChain.init(mainEvent);









    }






    @Transactional
    public List<SimpleFileInfo> getRecentFiles(SecurityContext securityContext, RequestContext requestContext, UUID projectId) throws Exception {
        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);

        StructureSnapshot snapshot = snapshotService.getSnapshot(project.getRoot().getId());

        return utils.getRecentFiles(snapshot);



    }

    /*
    todo - вопрос - создает ли чтение с диска запись в кеш?
     */
    @Transactional
    public FileDTO readFile(SecurityContext securityContext, RequestContext requestContext, UUID projectId, Long fileId) throws Exception{


        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);

        Optional<FileDTO> fileDTOFromCache = fileContentCache.read(fileId);

        // если кеш пустой, то мы должны сформировать для него запись

        if (fileDTOFromCache.isEmpty()){


            StructureSnapshot snapshot = snapshotService.getSnapshot(project.getRoot().getId());
            // извлекаем файл из снимка, если он есть
            Optional<FileReadOnly> check = utils.findAvailableFile(snapshot, fileId);



            if (check.isEmpty()){
                throw new IllegalStateException("Файл отсутствует, недоступен или не принадлежит проекту");
            }

            FileReadOnly dbFile = check.get();

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








}
