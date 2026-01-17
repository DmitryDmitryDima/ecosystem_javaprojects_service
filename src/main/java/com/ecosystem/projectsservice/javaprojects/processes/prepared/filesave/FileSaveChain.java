package com.ecosystem.projectsservice.javaprojects.processes.prepared.filesave;


import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventName;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.OutboxDeclarativeChain;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.markers.ProjectEvent;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
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
@ExternalResultName(event = ExternalEventName.JAVA_PROJECT_FILE_SAVE)
public class FileSaveChain extends OutboxDeclarativeChain<FileSaveEvent> {

    @Autowired
    private FileRepository fileRepository;


    @Override
    public void configure() {

    }

    @Override
    @Async("taskExecutor")
    @EventListener
    public void catchEvent(FileSaveEvent event) {
        System.out.println(event.getMessage());
        System.out.println(event.getContext());
        super.processEvent(event);
    }

    @Override
    public void compensationStrategy(FileSaveEvent event) {
        // Шаг, после которого произошла ошибка
        String step = event.getInternalData().getCurrentStep();
        System.out.println("compensation for "+step);
    }

    // связываем цепочку с конкретным типом выходного ивента
    @Override
    protected ExternalEvent<? extends ExternalEventContext> bindResultingEvent() {
        return new ProjectEvent();
    }


    @OpeningStep(name = "lockFile")
    @Message
    @Next(name="writeFileToDisk")
    public FileSaveEvent lockFile(FileSaveEvent fileSaveEvent){

        System.out.println("perform - lock file");



        fileSaveEvent.setMessage("готовим файл к записи");

        File file = transaction().execute((status -> {



            Optional<File> fileCheck = fileRepository.findByIdForUpdate(fileSaveEvent.getExternalData().getFileId());

            if (fileCheck.isEmpty()) throw new IllegalArgumentException("файл отсутствует");

            File fileEntity = fileCheck.get();

            if (fileEntity.getStatus()== FileStatus.WRITING){
                throw new IllegalStateException("файл занят другим процессом");

            }

            fileEntity.setStatus(FileStatus.WRITING); // пока статус writing - никто не может писать в файл


            return fileEntity;
        }));

        // обновляем tranfer объекты цепи для следующих шагов
        fileSaveEvent.getExternalData().setName(file.getName());
        fileSaveEvent.getExternalData().setPath(file.getConstructedPath());

        // конструируем полный путь
        fileSaveEvent.getInternalData()
                .setFilePath(
                        Path.of(fileSaveEvent.getInternalData().getProjectsPath(),
                file.getConstructedPath()).normalize().toString()
                );





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

        return fileSaveEvent;
    }








}
