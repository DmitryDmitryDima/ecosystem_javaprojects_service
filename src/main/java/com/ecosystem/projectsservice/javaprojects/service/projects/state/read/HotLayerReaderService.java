package com.ecosystem.projectsservice.javaprojects.service.projects.state.read;


import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.cache.CachedFile;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.service.cache.FileCache;
import com.ecosystem.projectsservice.javaprojects.service.external_values.StorageExternals;
import com.ecosystem.projectsservice.javaprojects.service.storage.StorageException;
import com.ecosystem.projectsservice.javaprojects.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/*
операции, связанные с чтением из "горячей" модели данных
 */
@Service
public class HotLayerReaderService implements HotLayerReader{


    // горячий слой
    @Autowired
    private FileCache fileCache;

    // холодный слой контента для подготовки горячего слоя
    @Autowired
    private StorageService storageService;

    @Autowired
    private StorageExternals storageExternals;


    @Autowired
    private SnapshotService snapshotService;







    @Override
    public FileDTO readFile(UUID projectId, UUID fileId) {


        Optional<CachedFile> cachedFileCheck = fileCache.get(fileId);

        CachedFile cachedFile = cachedFileCheck.orElseGet(() -> loadFromCold(projectId, fileId));

        // делаем прогрев
        fileCache.saveOrUpdate(cachedFile);


        System.out.println(cachedFile.getLastUpdate());


        return FileDTO.builder()
                .id(cachedFile.getId())
                .name(cachedFile.getName())
                .extension(cachedFile.getExtension())
                .content(cachedFile.getContent())
                .constructedPath(cachedFile.getConstructedPath())
                .projectId(projectId)
                .build();
    }

    @Override
    public List<FileDTO> getAllHotFilesFromList(List<UUID> files) {

        List<FileDTO> answer = new ArrayList<>();

        for (var id:files){
            Optional<CachedFile> cachedFileCheck = fileCache.get(id);
            cachedFileCheck.ifPresent(cachedFile -> answer.add(FileDTO.builder()
                    .content(cachedFile.getContent())
                    .extension(cachedFile.getExtension())
                    .name(cachedFile.getName())
                    .constructedPath(cachedFile.getConstructedPath())
                    .id(cachedFile.getId())
                    .projectId(cachedFile.getProjectId())
                    .lastUpdate(cachedFile.getLastUpdate())
                    .build()));
        }



        return answer;
    }


    private CachedFile loadFromCold(UUID projectId, UUID fileId){

        System.out.println("cold read");


        // проверяем базу данных

        Optional<FileReadOnly> dbCheck = snapshotService.getProjectFile(projectId, fileId);

        if (dbCheck.isEmpty()){
            throw new StateReadException("Файла не существует", "MISSING_FILE", HttpStatus.NOT_FOUND);
        }

        FileReadOnly metadata = dbCheck.get();

        if (metadata.isHidden() || metadata.getStatus() == FileStatus.REMOVING){
            throw new StateReadException("Файл недоступен для чтения", "FORBIDDEN_FILE", HttpStatus.FORBIDDEN);
        }

        String content;

        try {
            content = storageService.downloadContent(storageExternals.getStorageUserBucket(), fileId.toString());
        }

        catch (StorageException e){
            throw new StateReadException("Контент файла недоступен, попробуйте позже",
                    "MISSING_CONTENT",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }





        return CachedFile.builder()
                .id(fileId)
                .name(metadata.getName())
                .lastUpdate(Instant.now())
                .constructedPath(metadata.getConstructed_path())
                .version(0)
                .content(content)
                .extension(metadata.getExtension())
                .projectId(projectId)
                .build();
    }






}
