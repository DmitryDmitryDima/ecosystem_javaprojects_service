package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.processes.chains.AbstractOutboxChain;
import com.ecosystem.projectsservice.javaprojects.processes.chains.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save.FileSaveInfo;
import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save.FileSaveLockCreatedEvent;
import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.EventStatus;
import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.ProjectEvent;
import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.ProjectExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class FileSaveOutboxEventChain extends AbstractOutboxChain<FileSaveOutboxCompensationEvent, FileSaveInfo> {



    @Autowired
    private FileRepository fileRepository;




    // в методе инициации мы создаем макет данных для будущего результата всей цепочки. Помимо этого, мы производим первую публикацию в бд
    // если этот метод падает - пользователь получает мгновенный ответ с ошибкой
    @Override
    @Transactional
    public void init(SecurityContext securityContext, RequestContext requestContext, FileSaveInfo info) throws Exception{
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
    @Transactional
    @Async("taskExecutor")
    @EventListener
    public void lockFileEntity(FileSaveOutboxInitEvent initiation) throws Exception{


        try {
            Optional<File> fileCheck = fileRepository.findByIdForUpdate(initiation.getData().getFileId());

            if (fileCheck.isEmpty()) throw new IllegalArgumentException("файл отсутствует");

            File file = fileCheck.get();

            if (file.getStatus()== FileStatus.WRITING){
                throw new IllegalStateException("файл занят другим процессом");

            }

            file.setStatus(FileStatus.WRITING); // пока статус writing - никто не может писать в файл
            fileRepository.save(file);


            // формируем следующий шаг
            FileSaveOutboxLockCreatedEvent lockCreatedEvent = new FileSaveOutboxLockCreatedEvent(Path.of(initiation.getProjectsPath(),
                    file.getConstructedPath()).normalize().toString());

            initiation.getData().setName(file.getName());// запоминаем имя для ui
            initiation.getData().setPath(file.getConstructedPath());

            lockCreatedEvent.setData(initiation.getData());
            lockCreatedEvent.setContext(initiation.getContext());

            pushChain(lockCreatedEvent, initiation.getOutboxParent());

        }
        catch (Exception e){

            // формируем результирующий ивент на случай входа в компенсацию
            initiation.getData().setStatus(EventStatus.ERROR);

            ProjectEvent projectEvent = ProjectEvent.builder()
                    .event_type(getResultingEventName())
                    .message("Ошибка захвата файла: Причина "+e.getMessage())
                    .eventData(initiation.getData())
                    .context(initiation.getContext())
                    .build();


            errorProcessing(initiation,
                    new FileSaveOutboxCompensationEvent(initiation.getData().getFileId()),
                    projectEvent

            );


        }




    }





    // объединяю компенсацию и отправку сообщения об ошибке
    @Override
    @Transactional
    @Async("taskExecutor")
    @EventListener
    public void compensation(FileSaveOutboxCompensationEvent compensationEvent){

        Class<? extends ChainEvent> afterType = compensationEvent.getAfterEventTypeConverted();

        if (afterType == FileSaveOutboxInitEvent.class){
            // ошибка первого шага - не делаем ничего кроме разлочивания редиса
            System.out.println("change nothing in db");


        }

        // другие этапы




    }

    @Override
    public List<Class<? extends ChainEvent>> getAllSteps() {
        return List.of(
                FileSaveOutboxInitEvent.class,
                FileSaveOutboxLockCreatedEvent.class,
                FileSaveOutboxCompensationEvent.class);
    }

    @Override
    public String getResultingEventName() {
        return "java_project_file_save";
    }





}
