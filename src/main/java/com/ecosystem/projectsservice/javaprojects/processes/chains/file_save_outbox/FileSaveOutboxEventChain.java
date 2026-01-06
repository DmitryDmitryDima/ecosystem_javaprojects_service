package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.processes.chains.AbstractOutboxChain;
import com.ecosystem.projectsservice.javaprojects.processes.chains.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.EventName;
import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save.FileSaveInfo;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.*;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FileSaveOutboxEventChain extends AbstractOutboxChain<FileSaveInfo, FileSaveOutboxCompensationEvent, ProjectEvent> {



    @Autowired
    private FileRepository fileRepository;




    // в методе инициации мы создаем макет данных для будущего результата всей цепочки. Помимо этого, мы производим первую публикацию в бд
    // если этот метод падает - пользователь получает мгновенный ответ с ошибкой
    @Override
    public void init(SecurityContext securityContext, RequestContext requestContext, FileSaveInfo info) throws Exception{
        System.out.println("chain start");
        // готовим transfer object - event data
        FileSaveEventData eventData = FileSaveEventData
                .builder()
                .fileId(info.getFileId())
                .content(info.getContent())
                .build();

        // готовим transfer object - event context
        ProjectExternalEventContext context = ProjectExternalEventContext.builder()
                .correlationId(requestContext.getCorrelationId())
                .renderId(requestContext.getRenderId())
                .timestamp(Instant.now())
                .userUUID(securityContext.getUuid())
                .username(securityContext.getUsername())
                .projectId(info.getProjectId())
                .build();

        FileSaveOutboxInitEvent initiationEvent = new FileSaveOutboxInitEvent(info.getProjectsPath());
        initiationEvent.setData(eventData);
        initiationEvent.setContext(context);

        initiationEvent.setProjectsPath(info.getProjectsPath());


        pushChain(initiationEvent, null);

    }

    // todo блокировка редиса
    @Async("taskExecutor")
    @EventListener
    public void lockFileEntity(FileSaveOutboxInitEvent initiation){

        log.info("init event catched");

        try {
            File file = transaction().execute((status -> {

                Optional<File> fileCheck = fileRepository.findByIdForUpdate(initiation.getData().getFileId());

                if (fileCheck.isEmpty()) throw new IllegalArgumentException("файл отсутствует");

                File fileEntity = fileCheck.get();

                if (fileEntity.getStatus()== FileStatus.WRITING){
                    throw new IllegalStateException("файл занят другим процессом");

                }

                fileEntity.setStatus(FileStatus.WRITING); // пока статус writing - никто не может писать в файл


                return fileEntity;
            }));

            // формируем следующий шаг
            FileSaveOutboxLockCreatedEvent lockCreatedEvent = new FileSaveOutboxLockCreatedEvent(Path.of(initiation.getProjectsPath(),
                    file.getConstructedPath()).normalize().toString());

            initiation.getData().setName(file.getName());// запоминаем имя для ui
            initiation.getData().setPath(file.getConstructedPath());

            lockCreatedEvent.setData(initiation.getData());
            lockCreatedEvent.setContext(initiation.getContext());

            pushChainWithExternalMessage(lockCreatedEvent,
                    ProjectEvent.builder()
                            .status(EventStatus.PROCESSING)
                            .event_type(getResultingEventName())
                            .message("Синхронизация изменений")
                            .context(initiation.getContext())
                            .eventData(initiation.getData())
                            .build(),
                    initiation.getOutboxParent());
        }
        catch (Exception e){
            errorProcessing(initiation,

                    new FileSaveOutboxCompensationEvent(FileSaveOutboxLockCreatedEvent.class.getAnnotation(EventName.class).value(),
                            initiation.getData().getFileId()),

                    generateResult("ошибка работы с файлом на этапе захвата. Причина: "+e.getMessage(), EventStatus.ERROR,
                            initiation.getContext(),
                            initiation.getData())

            );
        }




    }


    @Async("taskExecutor")
    @EventListener
    public void writeToDisk(FileSaveOutboxLockCreatedEvent lockCreatedEvent){
        try {

            Files.writeString(Path.of(lockCreatedEvent.getFilePath()),
                    lockCreatedEvent.getData().getContent(),
                    StandardOpenOption.TRUNCATE_EXISTING
            );





            FileSaveOutboxFileWrittenEvent fileWrittenEvent = new FileSaveOutboxFileWrittenEvent();
            fileWrittenEvent.setContext(lockCreatedEvent.getContext());
            fileWrittenEvent.setData(lockCreatedEvent.getData());



            pushChainWithExternalMessage(fileWrittenEvent, ProjectEvent.builder()
                    .status(EventStatus.PROCESSING)
                    .event_type(getResultingEventName())
                    .message("Сохранение файла - запись в диск")
                    .context(lockCreatedEvent.getContext())
                    .eventData(lockCreatedEvent.getData())
                    .build(),
                    lockCreatedEvent.getOutboxParent() );



        }
        catch (Exception e){
            errorProcessing(lockCreatedEvent,
                    new FileSaveOutboxCompensationEvent(FileSaveOutboxLockCreatedEvent.class.getAnnotation(EventName.class).value(),
                            lockCreatedEvent.getData().getFileId()),

                    generateResult("ошибка работы с файлом на этапе записи. Причина: "+e.getMessage(), EventStatus.ERROR,
                            lockCreatedEvent.getContext(),
                            lockCreatedEvent.getData())

            );
        }
    }

    @Async
    @EventListener
    public void releaseFile(FileSaveOutboxFileWrittenEvent fileWrittenEvent){
        try {
            transaction().execute(status -> {
                Optional<File> fileCheck = fileRepository.findByIdForUpdate(fileWrittenEvent.getData().getFileId());

                fileCheck.ifPresent(file -> file.setStatus(FileStatus.AVAILABLE));

                return null;
            });

            sendFinalResult(
                    ProjectEvent.builder()
                            .status(EventStatus.SUCCESS)
                            .event_type(getResultingEventName())
                            .message("Файл сохранен успешно")
                            .context(fileWrittenEvent.getContext())
                            .eventData(fileWrittenEvent.getData())
                            .build(),

                    fileWrittenEvent.getOutboxParent()
            );
        }
        catch (Exception e){
            errorProcessing(fileWrittenEvent,
                    new FileSaveOutboxCompensationEvent(FileSaveOutboxLockCreatedEvent.class.getAnnotation(EventName.class).value(),
                            fileWrittenEvent.getData().getFileId()),

                    generateResult("ошибка бд"+e.getMessage(), EventStatus.ERROR,
                            fileWrittenEvent.getContext(),
                            fileWrittenEvent.getData())

            );
        }
    }









    // в компенсации мы можем определить, какой именно ивент выкинул ошибку
    @Override
    @Async("taskExecutor")
    @EventListener
    public void compensation(FileSaveOutboxCompensationEvent compensationEvent){

        endOutboxEntity(compensationEvent.getOutboxParent()); // todo подумай, как автоматизирвоать этот коллбэк (мб статус менять в обработчике?)

        Class<? extends ChainEvent> afterType = compensationEvent.getAfterEventTypeConverted();

        if (afterType == FileSaveOutboxInitEvent.class){
            // ошибка первого шага - не делаем ничего кроме разлочивания редиса
            System.out.println("change nothing in db");


        }

        if (afterType == FileSaveOutboxLockCreatedEvent.class || afterType == FileSaveOutboxFileWrittenEvent.class){
            System.out.println("here we should unlock db");
            try {
                transaction().execute((status -> {
                    Optional<File> fileCheck = fileRepository.findByIdForUpdate(compensationEvent.getFileId());
                    if (fileCheck.isPresent()){
                        File file = fileCheck.get();
                        file.setStatus(FileStatus.AVAILABLE);
                    }
                    return null;
                }));
            }
            catch (Exception e){
                log.info(e.getMessage());
            }
        }

        // другие этапы


    }

    @Override
    public List<Class<? extends ChainEvent>> getAllSteps() {
        return List.of(
                FileSaveOutboxInitEvent.class,
                FileSaveOutboxLockCreatedEvent.class,
                FileSaveOutboxFileWrittenEvent.class,
                FileSaveOutboxCompensationEvent.class);
    }

    @Override
    public String getResultingEventName() {
        return "java_project_file_save";
    }

    @Override
    public ProjectEvent generateResult(String message, EventStatus status, ExternalEventContext context, ExternalEventData data) {

        return ProjectEvent
                .builder()
                .eventData(data)
                .event_type(getResultingEventName())
                .message(message)
                .context(context)
                .status(status)
                .build();
    }


}
