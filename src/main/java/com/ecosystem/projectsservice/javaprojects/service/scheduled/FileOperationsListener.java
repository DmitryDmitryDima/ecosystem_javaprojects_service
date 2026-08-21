package com.ecosystem.projectsservice.javaprojects.service.scheduled;

import com.ecosystem.projectsservice.javaprojects.dto.projects.cache.CachedFile;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.service.cache.FileCache;
import com.ecosystem.projectsservice.javaprojects.service.external_values.StorageExternals;
import com.ecosystem.projectsservice.javaprojects.service.storage.StorageService;
import com.ecosystem.projectsservice.javaprojects.transport.broadcast.Broadcast;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromSystemContext;
import com.ecosystem.projectsservice.javaprojects.service.processes.files_processes.filesave.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromSystem;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// фоновые процессы, ассоциированные с файлами в проектах
@Service
public class FileOperationsListener {

















    @Autowired
    private FileCache fileCache;


    @Autowired
    private FileRepository fileRepository;










    @Autowired
    private Broadcast broadcast;

    @Autowired
    private StorageService storageService;

    @Autowired
    private StorageExternals storageExternals;

    @Autowired
    private TransactionTemplate transactionTemplate;




    private final ExecutorService uploadExecutor = Executors.newVirtualThreadPerTaskExecutor();




    // интервал должен быть меньше, чем cache ttl

    // todo после записи в диск мы должны изменить флаг written в кеше с проверкой version


    // todo выходит так, что мы каждый раз выгружаем контент в память, что не есть хорошо
    @Scheduled(fixedDelay = 30000)
    public void writeFileContentToDisk(){
        fileCache.scan().forEach(file->{


            // записываем в хранилище только те файлы, чей written = false
            // это поможет избежать ситуации,
            // где файлы записываются в хранилище без каких либо изменений
            if (!file.isWritten()){
                System.out.println("cold writing for "+file.getName());
                CompletableFuture.runAsync(()->uploadFileContent(file), uploadExecutor);
            }








        });
    }


    private void uploadFileContent(CachedFile file){

        // сохраняем в хранилище
        storageService.saveOrUpdate(storageExternals.getStorageUserBucket(),
                file.getId().toString(),
                file.getContent());


        // отправляем уведомление о том, что изменения сохранены
        broadcast.sendAsync(new Broadcast.EventBuilder()
                .useEvent(ProjectEventFromSystem::new)
                .withContext(()->ProjectEventFromSystemContext.builder().correlationId(UUID.randomUUID())
                        .origin("background disk writer process")
                        .timestamp(Instant.now())
                        .projectId(file.getProjectId()).build())
                .withData(()->{
                    FileSaveExternalData data = new FileSaveExternalData();
                    data.setFileId(file.getId());

                    data.setName(file.getName());
                    data.setExtension(file.getExtension());
                    return data;
                })
                .withType(ExternalEventType.JAVA_PROJECT_FILE_SAVE_SYSTEM)
                .withMessage("Данные сохранены на диск")
                .build());


        System.out.println(file.getVersion());


        fileCache.markAsWritten(file.getId(), file.getVersion());

        // время последней записи в кеш
        Instant lastUpdated = file.getLastUpdate();


        transactionTemplate.execute(status -> {
            Optional<File> entityCheck = fileRepository.findById(file.getId());

            entityCheck.ifPresent(entity->{
                entity.setUpdatedAt(lastUpdated);
            });

            return null;
        });




















    }








}
