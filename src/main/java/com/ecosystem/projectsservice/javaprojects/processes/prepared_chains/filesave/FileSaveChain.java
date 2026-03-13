package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.filesave;


import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.StructureSnapshot;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.service.cache.FileContentCache;
import com.ecosystem.projectsservice.javaprojects.service.projects.SnapshotService;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectActionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

// указывается state event, проходящий через всю очередь, и ивент результат
@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_FILE_SAVE)
public class FileSaveChain extends ControlledOutboxChain<FileSaveEvent> {

    @Autowired
    private FileRepository fileRepository;

    // обновляем кеш в конце цепочки
    @Autowired
    private FileContentCache<FileDTO, Long> fileContentCache;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private ProjectActionsUtils actionsUtils;




    @Override
    public void configure() {

    }



    @Override
    @Async("taskExecutor")
    @EventListener
    public void catchEvent(FileSaveEvent event) {



        super.processEvent(event);


    }

    @Override
    public void compensationStrategy(FileSaveEvent event) {
        // Шаг, после которого произошла ошибка
        String step = event.getInternalData().getCurrentStep();
        System.out.println("compensation for "+step);

        // нужно освободить файл. Примечание - файл не может быть изменен. если какой либо процесс занимает лок
        if (!step.equals("lockFile")){
            transaction().execute(status -> {
                Optional<File> fileCheck = fileRepository.findByIdForUpdate(event.getExternalData().getFileId());

                fileCheck.ifPresent(file -> file.setStatus(FileStatus.AVAILABLE));

                return null;
            });
        }
    }

    // связываем цепочку с конкретным типом выходного ивента
    @Override
    protected ExternalEvent<? extends ExternalEventContext> bindResultingEvent() {
        return new ProjectEventFromUser();
    }

    @Override
    protected void setProcessAssociations(FileSaveEvent event) {

    }





    @OpeningStep(name = "lockFile")
    @Message
    @Next(name="writeFileToDisk")
    @MaxDuration(time = 5)
    public FileSaveEvent lockFile(FileSaveEvent fileSaveEvent){

        System.out.println("perform - lock file");



        fileSaveEvent.setMessage("готовим файл к записи - проверяем зависимости");

        // транзакция осуществляет пессимистичную блокировку
        File file = transaction().execute((status -> {


            // проверка существования в базе
            Optional<File> fileCheck = fileRepository.findByIdForUpdate(fileSaveEvent.getExternalData().getFileId());

            if (fileCheck.isEmpty()) throw new IllegalArgumentException("файл отсутствует");





            File fileEntity = fileCheck.get();

            StructureSnapshot snapshot = snapshotService.getFullChildrenSnapshot(fileSaveEvent.getInternalData().getProjectRoot());
            Optional<FileReadOnly> presence = actionsUtils.findAvailableFile(snapshot, fileEntity.getId());

            if (presence.isEmpty()) throw new IllegalStateException("файл недоступен или не является частью проекта");

            // конструируем путь для записи в диск
            fileSaveEvent.getInternalData().setFilePath(Path.of(fileSaveEvent.getInternalData().getProjectsPath(),
                    fileEntity.getConstructedPath()).normalize().toString());

            // дополняем необходимые поля
            fileSaveEvent.getExternalData().setExtension(fileEntity.getExtension());
            fileSaveEvent.getExternalData().setName(fileEntity.getName());
            fileSaveEvent.getExternalData().setPath(fileEntity.getConstructedPath());

            // на выходе из пессимистично заблокированной транзакции файл будет иметь статус writing -
            // это автоматически защитит его от параллельного удаления или от удаления директории, в которой он находится
            fileEntity.setStatus(FileStatus.WRITING);


            return fileEntity;
        }));










        return fileSaveEvent;
    }

    @Step(name = "writeFileToDisk")
    @Message
    @MaxRetry(maxCount = 3)
    @Next(name = "releaseFile")
    public FileSaveEvent writeFileToDisk(FileSaveEvent fileSaveEvent) throws IOException {

        System.out.println("perform - write to disk");
        fileSaveEvent.setMessage("выполняем запись в диск");





        Files.writeString(Path.of(fileSaveEvent.getInternalData().getFilePath()),
                fileSaveEvent.getExternalData().getContent(),
                StandardOpenOption.TRUNCATE_EXISTING
        );




        return fileSaveEvent;


    }

    @EndingStep(name = "releaseFile")
    public FileSaveEvent releaseFile(FileSaveEvent fileSaveEvent){
        System.out.println("perform - release file");
        fileSaveEvent.setMessage("освобождаем файл");



        transaction().execute(status -> {
            Optional<File> fileCheck = fileRepository.findByIdForUpdate(fileSaveEvent.getExternalData().getFileId());

            fileCheck.ifPresent(file -> file.setStatus(FileStatus.AVAILABLE));

            return null;
        });

        // обновляем запись в кеше - чтобы с этого момента чтение было актуальным


        FileDTO fileDTO = FileDTO.builder()
                .content(fileSaveEvent.getExternalData().getContent())
                .constructedPath(fileSaveEvent.getExternalData().getPath())
                .id(fileSaveEvent.getExternalData().getFileId())
                .extension(fileSaveEvent.getExternalData().getExtension())
                .name(fileSaveEvent.getExternalData().getName())
                .projectId(fileSaveEvent.getContext().getProjectId())
                .ownerUUID(fileSaveEvent.getExternalData().getFileOwner())
                .build();

        try {
            fileContentCache.save(fileDTO.getId(), fileDTO);
        }
        catch (Exception e){
            e.printStackTrace();
        }



        return fileSaveEvent;
    }








}
