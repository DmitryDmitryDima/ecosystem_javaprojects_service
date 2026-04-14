package com.ecosystem.projectsservice.javaprojects.service.scheduled;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.service.ExternalValues;
import com.ecosystem.projectsservice.javaprojects.service.cache.external.FileCache;
import com.ecosystem.projectsservice.javaprojects.transport.broadcast.Broadcast;
import com.ecosystem.projectsservice.javaprojects.transport.broadcast.BroadcastException;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.ProjectEventFromSystemContext;
import com.ecosystem.projectsservice.javaprojects.service.processes.files.filesave.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromSystem;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.service.cache.CacheValueWrapper;
import com.ecosystem.projectsservice.javaprojects.service.cache.FileContentCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// фоновые процессы, ассоциированные с файлами в проектах
@Service
public class FileOperationsListener {









    private final long FILE_WRITE_PERIOD_OF_INACTIVITY_IN_SECONDS = 20;

    // время, через которое файловая запись в кеше считается просроченной
    private final static long FILE_CACHE_EXPIRATION_PERIOD_IN_SEC = 60*60;



    @Autowired
    private FileContentCache<FileDTO, Long> fileContentCache;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private FileCache fileCache;

    @Autowired
    private ExternalValues externalValues;





    @Autowired
    private Broadcast broadcast;




    // интервал должен быть меньше, чем cache ttl

    // todo после записи в диск мы должны изменить флаг written в кеше с проверкой version

    @Scheduled(fixedDelay = 30000)
    public void writeFileContentToDisk(){
        fileCache.scan().forEach(file->{
            performDiskWriting(file);

            try {
                broadcast.sendSync(new Broadcast.EventBuilder()
                        .useEvent(ProjectEventFromSystem::new)
                        .withContext(()->ProjectEventFromSystemContext.builder().correlationId(UUID.randomUUID())
                                .origin("background disk writer process")
                                .timestamp(Instant.now())
                                .projectId(file.getProjectId()).build())
                        .withData(()->{
                            FileSaveExternalData data = new FileSaveExternalData();
                            data.setFileOwner(file.getOwnerUUID());
                            data.setFileId(file.getId());
                            data.setPath(file.getConstructedPath());
                            data.setName(file.getName());
                            data.setExtension(file.getExtension());
                            return data;
                        })
                        .withType(ExternalEventType.JAVA_PROJECT_FILE_SAVE_SYSTEM)
                        .withMessage("Данные сохранены на диск")
                        .build());
            } catch (BroadcastException e) {
                throw new RuntimeException(e);
            }


        });
    }

    private void performDiskWriting(FileDTO file) {
        Path filePath = Path.of(externalValues.getUserStoragePath(),
                file.getOwnerUUID().toString(),
                "projects", file.getConstructedPath());

        try {
            Files.writeString(filePath, file.getContent(), StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("background write");
        }
        catch (Exception e){

        }


    }


    private boolean shouldWriteFile(CacheValueWrapper<FileDTO> entry){
        return (
                Duration.between(entry.getLastUpdate(),
                        Instant.now()).getSeconds()>FILE_WRITE_PERIOD_OF_INACTIVITY_IN_SECONDS
                        && Duration.between(entry.getLastUpdate(),
                        Instant.now()).getSeconds()<3*FILE_WRITE_PERIOD_OF_INACTIVITY_IN_SECONDS
        );
    }

    private void performDiskWrite(CacheValueWrapper<FileDTO> file){
        Path filePath = Path.of(externalValues.getUserStoragePath(),
                file.getValue().getOwnerUUID().toString(),
                "projects", file.getValue().getConstructedPath());



        boolean canWrite = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            Optional<File> dbCheck = fileRepository.findById(file.getValue().getId());

            return dbCheck.isPresent() && dbCheck.get().getStatus() == FileStatus.AVAILABLE;
        }));

        if (canWrite){
            try {
                Files.writeString(filePath, file.getValue().getContent(), StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("background write");
            }
            catch (Exception e){

            }
        }
    }

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    public void clearFileCache(){
        fileContentCache.removeExpiredWithPeriodInSec(FILE_CACHE_EXPIRATION_PERIOD_IN_SEC);
    }




    /*
    периодически записываем в диск данные кеша, при этом определяя, нужно ли это делать
     */
    @Scheduled(fixedDelay = 30000)
    public void fileDiskWriteOperations(){

        //System.out.println("disk write trigger");



        List<CacheValueWrapper<FileDTO>> files =  fileContentCache.readAllEntries();


        files.forEach(file->{

            // если прошло слишком мало или слишком много времени с момента последней активности (чтение или сохранение) - диск не трогаем
            if (!shouldWriteFile(file)){
                return;
            }


            try {


                performDiskWrite(file);

                broadcast.sendSync(new Broadcast.EventBuilder()
                        .useEvent(ProjectEventFromSystem::new)
                        .withContext(()->ProjectEventFromSystemContext.builder().correlationId(UUID.randomUUID())
                                .origin("background disk writer process")
                                .timestamp(Instant.now())
                                .projectId(file.getValue().getProjectId()).build())
                        .withData(()->{
                            FileSaveExternalData data = new FileSaveExternalData();
                            data.setFileOwner(file.getValue().getOwnerUUID());
                            data.setFileId(file.getValue().getId());
                            data.setPath(file.getValue().getConstructedPath());
                            data.setName(file.getValue().getName());
                            data.setExtension(file.getValue().getExtension());
                            return data;
                        })
                        .withType(ExternalEventType.JAVA_PROJECT_FILE_SAVE_SYSTEM)
                        .withMessage("Данные сохранены на диск")
                        .build());







            } catch (Exception e) {


            }








        });

    }


}
