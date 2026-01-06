package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.filesave;


import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.DeclarativeChain;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.markers.ProjectEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// указывается state event, проходящий через всю очередь, и ивент результат
@Service
@ResultingName(name = "file_save")
public class FileSaveChain extends DeclarativeChain<FileSaveEvent> {


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
    protected ExternalEvent bindResultingEvent() {
        return new ProjectEvent();
    }


    @OpeningStep(name = "lockFile")
    @Message
    @Next(name="writeFileToDisk")
    public FileSaveEvent lockFile(FileSaveEvent fileSaveEvent){




        return fileSaveEvent;
    }

    @Step(name = "writeFileToDisk")
    @Message
    @MaxRetry(maxCount = 3)
    @Next(name = "releaseFile")
    public FileSaveEvent writeFileToDisk(FileSaveEvent fileSaveEvent){

        return fileSaveEvent;
    }

    @EndingStep(name = "releaseFile")
    public FileSaveEvent releaseFile(FileSaveEvent fileSaveEvent){

        return fileSaveEvent;
    }








}
