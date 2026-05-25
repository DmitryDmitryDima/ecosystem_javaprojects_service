package com.ecosystem.projectsservice.javaprojects.service.processes.files.filesave;


import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.updates.CachedFileInvalidation;
import com.ecosystem.projectsservice.javaprojects.dto.projects.state.updates.ForcedSave;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
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

import java.util.List;
import java.util.Optional;


/*

Политика - никто из родителей не должен удаляться в моменте

сайд эффекты - полное обновление записи файла в кеше

 */
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


    @OpeningStep(name = "prepareFile")
    @Next(name = "lockFile")
    public void prepareFile(FileSaveEvent event){

        event.setMessage("готовим файл к записи");

        transaction().execute(status -> {


            Optional<File> fileCheck
                    = fileRepository.findByIdForUpdate(event.getExternalData().getFileId());

            if (fileCheck.isEmpty()) throw new IllegalArgumentException("файл отсутствует");

            File file = fileCheck.get();

            if (file.isHidden()) throw new IllegalStateException("вы не можете редактировать этот файл");

            if (file.getStatus()!=FileStatus.AVAILABLE)
                throw new IllegalStateException("Файл занят другим процессом");


            return null;
        });


    }





    @Step(name = "lockFile")
    @Message
    @Next(name="writeFileToStorage")
    public void lockFile(FileSaveEvent fileSaveEvent){





        fileSaveEvent.setMessage("блокируем файл");

        // транзакция осуществляет пессимистичную блокировку
        File file = transaction().execute((status -> {


            // проверка существования в базе
            Optional<File> fileCheck
                    = fileRepository.findByIdForUpdate(fileSaveEvent.getExternalData().getFileId());

            if (fileCheck.isEmpty()) throw new IllegalArgumentException("файл отсутствует");








            File fileEntity = fileCheck.get();

            if (fileEntity.getStatus()!=FileStatus.PREPARING_FOR_WRITING){
                throw new IllegalStateException("неподходящий статус для стадии блокировки");
            }


            List<DirectoryReadOnly> parents = snapshotService
                    .getParentsSnapshotDirectoriesOnly(fileEntity.getParent().getId());


            boolean containsRoot = false;

            for (var dir:parents){
                if (dir.getId().equals(fileSaveEvent.getInternalData().getProjectRoot())){
                    containsRoot = true;
                }

                if (dir.getStatus() == DirectoryStatus.PREPARING_FOR_REMOVAL
                        || dir.getStatus() == DirectoryStatus.REMOVING){

                    throw new IllegalStateException("одна из родительских директорий помечена на удаление");

                }
            }

            if (!containsRoot){
                throw new IllegalStateException("файл не принадлежит проекту");
            }



            // дополняем необходимые поля
            fileSaveEvent.getExternalData().setExtension(fileEntity.getExtension());
            fileSaveEvent.getExternalData().setName(fileEntity.getName());


            // на выходе из пессимистично заблокированной транзакции файл будет иметь статус writing -
            // это автоматически защитит его от параллельного удаления или от удаления директории, в которой он находится
            fileEntity.setStatus(FileStatus.WRITING);


            return fileEntity;
        }));



        // инвалидируем кеш
        hotLayerUpdater.fileInvalidation(new CachedFileInvalidation(file.getId()));











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


        try {
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
        catch (Exception e){

        }








    }








}
