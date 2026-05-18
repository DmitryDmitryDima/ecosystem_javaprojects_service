package com.ecosystem.projectsservice.javaprojects.service.processes.files.filesave;


import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.CachedFileInvalidation;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.ForcedSave;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.service.external_values.StorageExternals;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.update.HotLayerUpdater;
import com.ecosystem.projectsservice.javaprojects.service.storage.StorageService;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.state.read.SnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

// указывается state event, проходящий через всю очередь, и ивент результат
@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_FILE_SAVE)
public class FileSaveChain extends ControlledOutboxChain<FileSaveEvent> {

    @Autowired
    private FileRepository fileRepository;






    @Autowired
    private SnapshotService snapshotService;


    @Autowired
    private HotLayerUpdater hotLayerUpdater;

    @Autowired
    private StorageService storageService;

    @Autowired
    private StorageExternals externals;




    @Autowired
    private FileSaveChainCompensator compensator;






    @Override
    public void configure() {

    }



    @Override
    @Async("chainExecutor")
    @EventListener
    public void catchEvent(FileSaveEvent event) {



        super.processEvent(event);


    }

    @Override
    public void compensationStrategy(FileSaveEvent event) {
        compensator.compensation(event);
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
    @Next(name="writeFileToStorage")
    @MaxDuration(time = 5)
    public void lockFile(FileSaveEvent fileSaveEvent){





        fileSaveEvent.setMessage("готовим файл к записи - проверяем зависимости");

        // транзакция осуществляет пессимистичную блокировку
        File file = transaction().execute((status -> {


            // проверка существования в базе
            Optional<File> fileCheck = fileRepository.findByIdForUpdate(fileSaveEvent.getExternalData().getFileId());

            if (fileCheck.isEmpty()) throw new IllegalArgumentException("файл отсутствует");





            File fileEntity = fileCheck.get();


            Optional<FileReadOnly> presence = snapshotService
                    .getFileBelowDirectory(fileSaveEvent.getInternalData().getProjectRoot(), fileEntity.getId());

            if (presence.isEmpty()) throw new IllegalStateException("файл недоступен или не является частью проекта");



            // дополняем необходимые поля
            fileSaveEvent.getExternalData().setExtension(fileEntity.getExtension());
            fileSaveEvent.getExternalData().setName(fileEntity.getName());


            // на выходе из пессимистично заблокированной транзакции файл будет иметь статус writing -
            // это автоматически защитит его от параллельного удаления или от удаления директории, в которой он находится
            fileEntity.setStatus(FileStatus.WRITING);


            return fileEntity;
        }));



        // инвалидируем кеш
        hotLayerUpdater.onFileInvalidate(new CachedFileInvalidation(file.getId()));











    }

    @Step(name = "writeFileToStorage")
    @Message
    @MaxRetry(maxCount = 3)
    @Next(name = "releaseFile")
    public void writeFileToStorage(FileSaveEvent fileSaveEvent)  {


        fileSaveEvent.setMessage("выполняем запись в диск");

        storageService.saveOrUpdate(externals.getStorageUserBucket(),
                fileSaveEvent.getExternalData().getFileId().toString(),
                fileSaveEvent.getExternalData().getContent());

















    }

    @EndingStep(name = "releaseFile")
    public void releaseFile(FileSaveEvent fileSaveEvent){

        fileSaveEvent.setMessage("освобождаем файл");



        File saved = transaction().execute(status -> {
            Optional<File> fileCheck = fileRepository.findByIdForUpdate(fileSaveEvent.getExternalData().getFileId());

            if (fileCheck.isEmpty()) throw new IllegalStateException("сущности нет");
            File file = fileCheck.get();

            file.setStatus(FileStatus.AVAILABLE);

            return file;



        });

        // обновляем запись в кеше - чтобы с этого момента чтение было актуальным
        hotLayerUpdater.onForcedSave(new ForcedSave(FileDTO.builder()
                .id(saved.getId())
                .content(fileSaveEvent.getExternalData().getContent())
                .constructedPath(saved.getConstructedPath())
                .extension(saved.getExtension())
                .projectId(fileSaveEvent.getContext().getProjectId())
                .name(saved.getName())
                .build()));






    }








}
