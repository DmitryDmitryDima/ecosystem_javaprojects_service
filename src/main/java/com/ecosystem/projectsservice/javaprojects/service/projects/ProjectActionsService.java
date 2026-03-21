package com.ecosystem.projectsservice.javaprojects.service.projects;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.*;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.directories.DirectoryAddRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.directories.DirectoryMoveRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.directories.DirectoryRemovalRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files.FileAddRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files.FileMoveRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files.FileRemovalRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files.FileSaveRequest;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.ProjectParticipant;
import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_move.DirectoryMoveChain;
import com.ecosystem.projectsservice.javaprojects.transport.broadcast.Broadcast;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_add.DirectoryAddChain;
import com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_removal.DirectoryRemovalChain;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.file_add.FileAddChain;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.file_move.FileMoveChain;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.file_removal.FileRemovalChain;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.filesave.FileSaveChain;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.filesave.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.TriggerAnswer;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.TriggersAggregator;
import com.ecosystem.projectsservice.javaprojects.service.ExternalValues;
import com.ecosystem.projectsservice.javaprojects.service.cache.FileContentCache;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectActionsUtils;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;




// ответственность внешнего сервиса - проверка прав. ответственность асинхронных внутренних цепочек - внутренние операции с бд, диском и кешем
// те данные, что могут быть сконструированы исходя из проверки, вставляем сразу


// todo данный слой может быть оптимизирован на этапе валидации - для этого можно ввести access token,
//  который будет жить мало и инвалидироваться при удалении пользователя из проекта
//  token будет работать с redis, передаваться через http only cookie

@Service
public class ProjectActionsService {







    @Autowired
    private ProjectActionsUtils utils;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private ProjectAccessValidator accessValidator;

    @Autowired
    private ProjectActionsEventBuilder eventBuilder;

    @Autowired
    private TriggersAggregator triggersAggregator;






    @Autowired
    private FileContentCache<FileDTO, Long> fileContentCache;




    @Autowired
    private Broadcast broadcast;


    // процессы
    @Autowired
    private FileSaveChain fileSaveChain;

    @Autowired
    private FileRemovalChain fileRemovalChain;

    @Autowired
    private FileAddChain fileAddChain;

    @Autowired
    private DirectoryAddChain directoryAddChain;

    @Autowired
    private DirectoryRemovalChain directoryRemovalChain;

    @Autowired
    private FileMoveChain fileMoveChain;

    @Autowired
    private DirectoryMoveChain directoryMoveChain;




    @Autowired
    private ExternalValues externalValues;



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

        utils.generateStructureForDTO(project.getRoot().getId(), projectDTO, snapshotService.getFullChildrenSnapshot(project.getRoot().getId()));

        return projectDTO;
    }






    // todo механизм автосохранения не полагается на цепочку, так как работает только с redis
    @Transactional
    public void autosave(SecurityContext securityContext,
                         RequestContext requestContext,
                         UUID projectId,
                         FileSaveRequest request) throws Exception{



        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);

        // безопасно читаем файл из снимка бд - при статусе available его можно писать в кеш


        StructureSnapshot snapshot = snapshotService.getFullChildrenSnapshot(project.getRoot().getId());

        // извлекаем файл из снимка, если он есть
        Optional<FileReadOnly> check = utils.findAvailableFile(snapshot, request.getFileId());



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


        fileContentCache.save(request.getFileId(), fileDTO);

        broadcast.sendSync(
                new Broadcast.EventBuilder().useEvent(ProjectEventFromUser::new)
                .withContext(()->ProjectEventFromUserContext.from(securityContext, requestContext, project, null, null))
                .withData(()->{
                    FileSaveExternalData externalData = new FileSaveExternalData();
                    externalData.setContent(request.getContent());
                    externalData.setFileId(request.getFileId());
                    return externalData;}).withType(ExternalEventType.JAVA_PROJECT_FILE_SAVE)
                .withMessage("Файл сохранен").build());




    }

    @Transactional
    public void addDirectory(SecurityContext securityContext,
                             RequestContext requestContext,
                             UUID projectId,
                             DirectoryAddRequest directoryAddRequest) throws Exception{

        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);



        directoryAddChain.init(eventBuilder.buildDirectoryAddEvent(securityContext, requestContext, project, directoryAddRequest));


    }




    @Transactional
    public void removeDirectory(SecurityContext securityContext, RequestContext requestContext, UUID projectId, DirectoryRemovalRequest request)

    throws Exception
    {

        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);

        directoryRemovalChain.init(eventBuilder.buildDirectoryRemovalEvent(securityContext, requestContext, project, request));




    }

    @Transactional
    public void addFile(SecurityContext securityContext,
                        RequestContext requestContext,
                        UUID projectId,
                        FileAddRequest fileAddRequest) throws Exception {


        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);


        fileAddChain.init(eventBuilder.buildFileAddEvent(securityContext, requestContext, project, fileAddRequest));

    }

    @Transactional
    public void moveFile(SecurityContext securityContext, RequestContext requestContext, UUID projectId, FileMoveRequest fileMoveRequest)
    throws Exception
    {

        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);

        if (project.getEntryPoint().getId().equals(fileMoveRequest.getFileId())){
            throw new IllegalStateException("Файл является точкой входа в проекте и не может быть перемещен");
        }

        fileMoveChain.init(eventBuilder.buildFileMoveEvent(securityContext, requestContext, project, fileMoveRequest));

    }

    @Transactional
    public void moveDirectory(SecurityContext securityContext,
                              RequestContext requestContext,
                              UUID projectId,
                              DirectoryMoveRequest directoryMoveRequest) throws Exception{

        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);


        directoryMoveChain.init(eventBuilder.buildDirectoryMoveEvent(securityContext, requestContext, project, directoryMoveRequest));




    }


    @Transactional
    public void removeFile(SecurityContext securityContext,
                           RequestContext requestContext,
                           UUID projectId,
                           FileRemovalRequest request) throws Exception{

        System.out.println(requestContext.getCorrelationId());

        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);

        // быстрая проверка - если файл - часть конфигурации, нужно попросить пользователя его изменить
        if (project.getEntryPoint().getId().equals(request.getFileId())){
            throw new IllegalStateException("файл входит в выбранный конфиг запуска");
        }






        fileRemovalChain.init(eventBuilder.buildFileRemovalEvent(securityContext, requestContext, project, request));






    }

    // метод форсированной записи файла в диск - гарантирует согласованность данных во всех связанных с файлом слоях - диск, бд, кеш
    // используем outbox цепочку - операция сложная, затрагивает несколько систем сразу
    @Transactional
    public void saveFile(SecurityContext securityContext,
                         RequestContext requestContext,
                         UUID projectId,
                         FileSaveRequest request) throws Exception {

        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);






        fileSaveChain.init(eventBuilder.buildFileSaveEvent(securityContext, requestContext, project, request));









    }






    @Transactional
    public List<SimpleFileInfo> getRecentFiles(SecurityContext securityContext, RequestContext requestContext, UUID projectId) throws Exception {
        Project project = accessValidator.validateAccess(securityContext, requestContext, projectId);

        StructureSnapshot snapshot = snapshotService.getFullChildrenSnapshot(project.getRoot().getId());

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


            StructureSnapshot snapshot = snapshotService.getFullChildrenSnapshot(project.getRoot().getId());
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
                Path path = ProjectUtils.constructPathToFile(externalValues.getUserStoragePath(), project, dbFile.getConstructed_path());
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
