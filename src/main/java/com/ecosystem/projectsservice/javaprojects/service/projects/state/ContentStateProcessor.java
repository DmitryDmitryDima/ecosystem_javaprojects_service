package com.ecosystem.projectsservice.javaprojects.service.projects.state;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.Autosave;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.ForcedSave;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.service.ExternalValues;
import com.ecosystem.projectsservice.javaprojects.service.cache.external.FileCache;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.filesave.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.service.projects.SnapshotService;
import com.ecosystem.projectsservice.javaprojects.transport.broadcast.Broadcast;

import com.ecosystem.projectsservice.javaprojects.transport.broadcast.BroadcastException;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Optional;

// данный класс предоставляет хуки, вызываемые при совершении каких либо действий с кодовой базой
// хуки должны получить всю необходимую для изменения состояния/состояний информацию, поэтому выделяем отдельные dto
// помимо хуков, процессор предоставляет непосредственный доступ к динамическому контенту
@Service
public class ContentStateProcessor {


    @Autowired
    private Broadcast broadcast;

    // сервис для db валидации
    @Autowired
    private SnapshotService snapshotService;

    // файловый кеш
    // todo собственный dto с кешированным файловым api и полями управления
    @Autowired
    private FileCache fileCache;



    @Autowired
    private CodeService codeService;

    @Autowired
    private ExternalValues externalValues;




    // читаем файл. Если его нет в кеше - валидируем через дб и читаем с диска,
    // при этом внося запись в кеш, если файл - AVAILABLE
    public FileDTO readFile(Long fileId, ProjectDTO projectDTO)
    {

        Optional<FileDTO> cacheCheck = fileCache.get(fileId);

        if (cacheCheck.isEmpty()){

            System.out.println("FILE READ DB VALIDATION");
            // db валидация
            Optional<FileReadOnly> fileCheck =  snapshotService
                    .getFileBelowDirectory(projectDTO.getRoot(), fileId);

            if (fileCheck.isEmpty()){
                throw new IllegalStateException("Файл не принадлежит проекту");


            }

            FileReadOnly file = fileCheck.get();

            // нельзя прочитать скрытый или удаляемый файл
            if (file.isHidden() || file.getStatus()== FileStatus.REMOVING){
                throw new IllegalStateException("Файл недоступен для записи");

            }

            // готовим dto
            FileDTO fileDTO = FileDTO.builder()
                    .name(file.getName())
                    .extension(file.getExtension())
                    .constructedPath(file.getConstructed_path())
                    .id(file.getId())
                    .ownerUUID(projectDTO.getAuthor())
                    .projectId(projectDTO.getId())
                    .build();

            // читаем контент с диска



            try {
                Path path = ProjectUtils.constructPathToFile(externalValues.getUserStoragePath(),
                        projectDTO.getAuthor(), file.getConstructed_path());
                fileDTO.setContent(ProjectUtils.readFile(path));

            }
            catch (Exception e){
                throw new IllegalStateException("Ошибка чтения файла. Причина: "+e.getMessage());
            }

            // сохраняем в кеш
            fileCache.saveOrUpdate(fileDTO);

            return fileDTO;
        }
        else {
            System.out.println("FILE READ CACHE ORIGIN");
            return cacheCheck.get();

        }


    }




    // отличие от autosave в том, что не требуется валидация и рассылка,
    // так как это произошло в цепочке
    public void onForcedSave(ForcedSave forcedSave){
        // сохраняем dto в кеш
        fileCache.saveOrUpdate(forcedSave.getFileDTO());
    }








    // данное событие провоцирует точечное изменение в кешах, broadcast рассылку
    // если записи в кеше нет, то проверяется статус файла в бд,
    // после чего происходит создание новой записи

    public void onAutosave(Autosave autosave){

        /* шаг 1 - проверяем, есть ли запись в кеше. Держим в голове, что любая операция/цепочка,
         меняющая состояние файла/файлов - обязана инвалидировать затронутую сущность,
         тем самым провоцируя db валидацию через snapshot иерархии
         */

        boolean result = fileCache.updateContent(autosave.getFileId(), autosave.getContent());

        System.out.println(result+" CONTENT FIELD UPDATED IN CACHE");

        if (!result){
            // db валидация
            Optional<FileReadOnly> fileCheck =  snapshotService
                    .getFileBelowDirectory(autosave.getProjectRoot(), autosave.getFileId());

            if (fileCheck.isEmpty()){
                throw new IllegalStateException("Файл не принадлежит проекту");


            }

            FileReadOnly file = fileCheck.get();

            if (file.isHidden() || file.getStatus()!= FileStatus.AVAILABLE){
                throw new IllegalStateException("Файл недоступен для записи");

            }

            // готовим dto. при операции чтения это dto будет прочтено из файлового кеша
            FileDTO fileDTO = FileDTO.builder()
                    .content(autosave.getContent())
                    .constructedPath(file.getConstructed_path())
                    .id(file.getId())
                    .extension(file.getExtension())
                    .name(file.getName())
                    .projectId(autosave.getProjectId())
                    .ownerUUID(autosave.getProjectOwner())
                    .build();

            // сохраняем dto в кеш
            fileCache.saveOrUpdate(fileDTO);
        }

        // рассылаем ивент
        try {
            broadcast.sendSync(
                    new Broadcast.EventBuilder().useEvent(ProjectEventFromUser::new)
                            .withContext(()->ProjectEventFromUserContext
                                    .from(autosave.getSecurityContext(),
                                            autosave.getRequestContext(),
                                            autosave.getProjectId(),
                                            null,
                                            null))
                            .withData(()->{
                                FileSaveExternalData externalData = new FileSaveExternalData();
                                externalData.setContent(autosave.getContent());
                                externalData.setFileId(autosave.getFileId());
                                return externalData;}).withType(ExternalEventType.JAVA_PROJECT_FILE_SAVE)
                            .withMessage("Файл сохранен").build());
        } catch (BroadcastException e) {
            throw new RuntimeException(e);
        }


    }







}
