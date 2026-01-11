package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.filesave;


import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.DeclarativeChain;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.markers.ProjectEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// указывается state event, проходящий через всю очередь, и ивент результат
@Service
@ExternalResultName(name = "file_save")
public class FileSaveChain extends DeclarativeChain<FileSaveEvent> {


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
        fileSaveEvent.setMessage("lock file");





        return fileSaveEvent;
    }

    @Step(name = "writeFileToDisk")
    @Message
    @MaxRetry(maxCount = 3)
    @Next(name = "releaseFile")
    public FileSaveEvent writeFileToDisk(FileSaveEvent fileSaveEvent){

        System.out.println("perform - write to disk");
        fileSaveEvent.setMessage("write to disk");


        return fileSaveEvent;


    }

    @EndingStep(name = "releaseFile")
    public FileSaveEvent releaseFile(FileSaveEvent fileSaveEvent){
        System.out.println("perform - release file");
        fileSaveEvent.setMessage("release file");

        return fileSaveEvent;
    }








}
