package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save;

import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.processes.external_queue.*;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Optional;

// сохранение с блокировкой бд, цель - запись в диск
// цепочка вызывается со стороны пользователя и со стороны фонового процесса
@Service
public class FileSaveEventChain {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ObjectMapper objectMapper;


    private static final String resultingEventName = "java_project_file_save";


    @Async("taskExecutor")
    public void initChain(SecurityContext securityContext, RequestContext requestContext, FileSaveInfo info){

        // готовим transfer object - event data
        FileSaveEventData eventData = FileSaveEventData
                .builder()
                .fileId(info.getFileId())
                .content(info.getContent())
                .build();

        // готовим transfer object - event context
        ProjectEventContext context = ProjectEventContext.builder()
                .correlationId(requestContext.getCorrelationId())
                .renderId(requestContext.getRenderId())
                .timestamp(Instant.now())
                .userUUID(securityContext.getUuid())
                .username(securityContext.getUsername())
                .projectId(info.getProjectId())
                .build();

        FileSaveInitiationEvent initiationEvent = new FileSaveInitiationEvent(this, info.getProjectsPath());
        initiationEvent.setData(eventData);
        initiationEvent.setContext(context);


        System.out.println("file save chain init "+initiationEvent);
        System.out.println("=======================================");

        try {
            String value = objectMapper.writeValueAsString(initiationEvent);
            FileSaveInitiationEvent parsed = objectMapper.readValue(value, FileSaveInitiationEvent.class);
            System.out.println(objectMapper.writeValueAsString(parsed)+" from");

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        //publisher.publishEvent(initiationEvent);




    }

    // db lock for atomicity
    // file has an optimistic lock mechanism
    @Transactional
    @EventListener
    public void lockFileEntity(FileSaveInitiationEvent initiation){

        try {
            Optional<File> fileCheck = fileRepository.findById(initiation.getData().getFileId());
            if (fileCheck.isEmpty()){
                throw new IllegalStateException("файл не найден");
            }



            File file = fileCheck.get();

            if (file.getStatus().equals(FileStatus.WRITING)){
                throw new IllegalStateException("файл недоступен для записи");
            }

            file.setStatus(FileStatus.WRITING); // пока статус writing - никто не может писать в файл
            fileRepository.save(file);

            initiation.getData().setName(file.getName());// запоминаем имя для ui
            initiation.getData().setPath(file.getConstructedPath());

            FileSaveLockCreatedEvent fileSaveLockCreatedEvent = new FileSaveLockCreatedEvent(this,
                            Path.of(initiation.getProjectsPath(),
                            file.getConstructedPath()).normalize().toString());

            fileSaveLockCreatedEvent.setData(initiation.getData());
            fileSaveLockCreatedEvent.setContext(initiation.getContext());


            System.out.println("FILE DB LOCK PERFORMED "+fileSaveLockCreatedEvent);
            System.out.println("=================================================");

            publisher.publishEvent(fileSaveLockCreatedEvent);




        }

        catch (Exception e){
            sendFailedResult("Ошибка проверки файла при попытке сохрания изменений - "+e.getMessage(),
                    initiation.getContext(), initiation.getData());
        }
    }

    // пишем на диск
    @EventListener
    public void writeToDisk(FileSaveLockCreatedEvent lockCreatedEvent){

        System.out.println("I will write "+lockCreatedEvent.getData().getContent()+" to file on address "+lockCreatedEvent.getFilePath());
        System.out.println("==========================================================================================================");

        try {


            Files.writeString(Path.of(lockCreatedEvent.getFilePath()),
                    lockCreatedEvent.getData().getContent(),
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            FileWrittenEvent fileWrittenEvent = new FileWrittenEvent(this);
            fileWrittenEvent.setContext(lockCreatedEvent.getContext());
            fileWrittenEvent.setData(lockCreatedEvent.getData());
            publisher.publishEvent(fileWrittenEvent);


        }
        catch (Exception e){
            FileSaveCompensationEvent compensationEvent = new FileSaveCompensationEvent(this, lockCreatedEvent.getData().getFileId());
            publisher.publishEvent(compensationEvent);
            sendFailedResult("ошибка работы с диском: "+e.getMessage(), lockCreatedEvent.getContext(), lockCreatedEvent.getData());
        }

    }


    // делаем файл доступным для записи

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @EventListener
    public void releaseFile(FileWrittenEvent fileWrittenEvent){

        try {
            Optional<File> fileCheck = fileRepository.findById(fileWrittenEvent.getData().getFileId());
            if (fileCheck.isEmpty()){
                throw new IllegalStateException("файл не найден");
            }



            File file = fileCheck.get();

            file.setStatus(FileStatus.AVAILABLE);
            fileRepository.save(file);

            System.out.println("db release performed "+fileWrittenEvent);
            System.out.println("========================================");

            sendSuccessResult("Файл сохранен", fileWrittenEvent.getContext(), fileWrittenEvent.getData());
        }

        catch (Exception e){
            sendFailedResult("Ошибка при смене статуса файла: "+e.getMessage(), fileWrittenEvent.getContext(), fileWrittenEvent.getData());
            FileSaveCompensationEvent fileSaveCompensationEvent = new FileSaveCompensationEvent(this,fileWrittenEvent.getData().getFileId());
            publisher.publishEvent(fileSaveCompensationEvent);
        }

    }



    // суть компенсации для данной цепочки - освободить файл
    // если бы не было after commit - асинхронный метод бы работал со старым состоянием БД
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskExecutor")
    public void compensation(FileSaveCompensationEvent compensationEvent){
        try {
            System.out.println("compensation");
            Optional<File> fileCheck = fileRepository.findById(compensationEvent.getFileId());

            if (fileCheck.isEmpty()){
                throw new IllegalStateException("файл не найден");
            }

            File file = fileCheck.get();


            file.setStatus(FileStatus.AVAILABLE);
            fileRepository.save(file);






        }
        catch (Exception e){
            // todo логирование
            e.printStackTrace();
        }
    }


    private void sendFailedResult(String message, ProjectEventContext context, FileSaveEventData data){
        data.setStatus(FileSaveStatus.FAIL);
        sendResult(message, context, data);
    }

    private void sendSuccessResult(String message, ProjectEventContext context, FileSaveEventData data){
        data.setStatus(FileSaveStatus.SUCCESS);
        sendResult(message, context, data);
    }

    private void sendResult(String message, ProjectEventContext context, EventData data){
        try {
            ProjectEvent projectEvent = ProjectEvent.builder()
                    .eventData(data)
                    .event_type(resultingEventName)
                    .context(context)
                    .message(message)
                    .build();
            publisher.publishEvent(projectEvent);
        }
        catch (Exception e){
            // ошибка тут должна компенсироваться в будущем
            e.printStackTrace();
        }
    }
}
