package com.ecosystem.projectsservice.javaprojects.service.projects.state.update;


import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.cache.CachedFile;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.Autosave;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.CachedFileInvalidation;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.ForcedSave;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.ProjectStructureInvalidation;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.service.cache.FileCache;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.filesave.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.read.SnapshotService;
import com.ecosystem.projectsservice.javaprojects.service.storage.StorageService;
import com.ecosystem.projectsservice.javaprojects.transport.broadcast.Broadcast;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromUserContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;


// хуки для операций, означающих внесение изменений в горячий слой
@Service
public class HotLayerUpdaterService implements HotLayerUpdater {



    @Autowired
    private Broadcast broadcast;

    @Autowired
    private FileCache fileCache;



    @Autowired
    private SnapshotService snapshotService;


    @Override
    public void onAutosave(Autosave autosave) {


        boolean result = fileCache
                .updateContent(autosave.getFileId(), autosave.getContent());



        if (!result){

            System.out.println("cold autosave");
            CachedFile cachedFile = coldAutosave(autosave);

            // новая запись в кеше
            fileCache.saveOrUpdate(cachedFile);
        }

        else {
            System.out.println("hot autosave");
        }



        broadcast.sendSync(
                new Broadcast.EventBuilder().useEvent(ProjectEventFromUser::new)
                        .withContext(()-> ProjectEventFromUserContext
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


    }

    // если запись в кеше отсутствует
    private CachedFile coldAutosave(Autosave autosave){
        // проверяем базу данных

        Optional<FileReadOnly> dbCheck = snapshotService
                .getProjectFile(autosave.getProjectId(), autosave.getFileId());

        if (dbCheck.isEmpty()){
            throw new StateUpdateException("Файла не существует", "MISSING_FILE", HttpStatus.NOT_FOUND);
        }

        FileReadOnly metadata = dbCheck.get();

        // writing процесс в конце делает свою (согласованную) запись в кеш, поэтому тут - блок
        if (metadata.isHidden() || metadata.getStatus() == FileStatus.REMOVING
                || metadata.getStatus() == FileStatus.WRITING) {
            throw new StateUpdateException("Файл недоступен для обновления",
                    "FORBIDDEN_FILE", HttpStatus.FORBIDDEN);
        }



        return CachedFile.builder()
                .id(autosave.getFileId())
                .name(metadata.getName())
                .lastUpdate(Instant.now())
                .constructedPath(metadata.getConstructed_path())
                .version(0)
                .content(autosave.getContent())
                .extension(metadata.getExtension())
                .projectId(autosave.getProjectId())
                .build();




    }



    @Override
    public void onForcedSave(ForcedSave save) {
        FileDTO file = save.getFileDTO();

        CachedFile cachedFile = CachedFile.builder()
                .id(file.getId())
                .name(file.getName())
                .lastUpdate(Instant.now())
                .constructedPath(file.getConstructedPath())
                .version(0)
                .content(file.getContent())
                .extension(file.getExtension())
                .projectId(file.getProjectId())
                .build();

        fileCache.saveOrUpdate(cachedFile);
    }

    @Override
    public void onFileInvalidate(CachedFileInvalidation fileInvalidation) {
        fileCache.delete(fileInvalidation.getFileId());
    }

    @Override
    public void onProjectStructureInvalidate(ProjectStructureInvalidation structureInvalidation) {

    }
}
