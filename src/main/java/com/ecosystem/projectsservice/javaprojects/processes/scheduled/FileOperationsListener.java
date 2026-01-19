package com.ecosystem.projectsservice.javaprojects.processes.scheduled;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.broadcastable_action.ActionResult;
import com.ecosystem.projectsservice.javaprojects.processes.broadcastable_action.BroadcastableAction;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ProjectEventFromSystemContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.FileSaveExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.ProjectEventFromSystem;
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

// фоновые процессы, ассоциированные с файлами в проектах
@Service
public class FileOperationsListener {


    @Value("${storage.user}")
    private String userStoragePath;



    private final long FILE_WRITE_PERIOD_OF_INACTIVITY_IN_SECONDS = 20;



    @Autowired
    private FileContentCache<FileDTO, Long> fileContentCache;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private BroadcastableAction broadcast;


    private boolean shouldWriteFile(CacheValueWrapper<FileDTO> entry){
        return (
                Duration.between(entry.getLastUpdate(),
                        Instant.now()).getSeconds()>FILE_WRITE_PERIOD_OF_INACTIVITY_IN_SECONDS
                        && Duration.between(entry.getLastUpdate(), Instant.now()).getSeconds()<3*FILE_WRITE_PERIOD_OF_INACTIVITY_IN_SECONDS
        );
    }

    private void performDiskWrite(CacheValueWrapper<FileDTO> file){
        Path filePath = Path.of(userStoragePath,
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

    private ActionResult<ProjectEventFromSystemContext, FileSaveExternalData> performFileDiskWrite(CacheValueWrapper<FileDTO> file){
        Path filePath = Path.of(userStoragePath,
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

        ProjectEventFromSystemContext context = ProjectEventFromSystemContext.builder()
                .correlationId(UUID.randomUUID())
                .origin("background disk writer process")
                .timestamp(Instant.now())
                .participants(List.of()) // не нужны, адресат - комната
                .projectId(file.getValue().getProjectId())
                .build();

        FileSaveExternalData data = new FileSaveExternalData();
        data.setFileOwner(file.getValue().getOwnerUUID());
        data.setFileId(file.getValue().getId());
        data.setPath(file.getValue().getConstructedPath());
        data.setName(file.getValue().getName());
        data.setExtension(file.getValue().getExtension());


        return new ActionResult<>(context, data, "Данные записаны на диск");
    }


    /*
    периодически записываем в диск
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


                broadcast.statelessAction(()-> performDiskWrite(file))
                        .withContext(()->{
                            // не нужны, адресат - комната
                            return ProjectEventFromSystemContext.builder()
                                            .correlationId(UUID.randomUUID())
                                            .origin("background disk writer process")
                                            .timestamp(Instant.now())
                                            .participants(List.of()) // не нужны, адресат - комната
                                            .projectId(file.getValue().getProjectId())
                                            .build();
                        })


                        .withData(()->{
                            FileSaveExternalData data = new FileSaveExternalData();
                            data.setFileOwner(file.getValue().getOwnerUUID());
                            data.setFileId(file.getValue().getId());
                            data.setPath(file.getValue().getConstructedPath());
                            data.setName(file.getValue().getName());
                            data.setExtension(file.getValue().getExtension());
                            return data;
                        })
                        .withEvent(ProjectEventFromSystem::new)
                        .withType(ExternalEventType.JAVA_PROJECT_FILE_SAVE_SYSTEM)
                        .withMessage("Данные записаны")
                        .execute();


                /*
                broadcast.createAction(()-> performFileDiskWrite(file))
                        .withExternalEventCategory(new ProjectEventFromSystem())
                        .withExternalEventType(ExternalEventType.JAVA_PROJECT_FILE_SAVE_SYSTEM)
                        .execute();

                 */

            } catch (Exception e) {


            }








        });

    }


}
