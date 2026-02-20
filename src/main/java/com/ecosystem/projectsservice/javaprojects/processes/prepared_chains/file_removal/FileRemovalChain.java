package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_removal;

import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.enums.StepTimeUnit;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.triggers.SimpleTriggerData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.*;
import com.ecosystem.projectsservice.javaprojects.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;


/*
todo инвалидация кеша предложек?
 */
@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_FILE_REMOVAL)
public class FileRemovalChain extends ControlledOutboxChain<FileRemovalEvent> {

    @Autowired
    private FileRepository fileRepository;

    @Override
    protected ExternalEvent<? extends ExternalEventContext> bindResultingEvent() {
        return new ProjectEventFromUser();
    }



    @Override
    protected void setProcessAssociations(FileRemovalEvent event) {

    }

    @Override
    @Async("taskExecutor")
    @EventListener
    public void catchEvent(FileRemovalEvent event) {
        super.processEvent(event);
    }

    @OpeningStep(name = "polling")
    @Next(name = "blockFile")
    public void polling(FileRemovalEvent event){



        System.out.println("polling phase - file removal");

        event.setMessage("Запрос на удаление файла от: "+event.getContext().getUsername());


        Function<Map<String, TriggerAnswer>, Boolean> onFeedStrategy = (answers)->{
            System.out.println("зарегистрирован ответ для фазового триггера. Текущие ответы "+answers);

            for (TriggerAnswer answer:answers.values()){
                // демонстрация мгновенного отказа
                if (answer.isDecision()&& answer.getContent().equals("No")){
                    event.setMessage("отказ в удалении файла. Не получено одобрение других участников");
                    event.getInternalData().setCompensationPhase(true);
                    return true;
                }
            }
            return false;
        };

        Function<Map<String, TriggerAnswer>, Boolean> activityPollingPhaseStrategy = (answers)->{

            System.out.println("activity check phase");
            for (TriggerAnswer answer:answers.values()){
                // если обнаружен кто то, кто не принял решение. ждем его
                if (!answer.isDecision()){
                    return false;
                }
            }
            // если все согласны, то очередь выполняет следующий шаг
            return true;
        };

        // конечная фаза - тут необходимо принять решение о том, продолжать ли цепочку
        Function<Map<String, TriggerAnswer>, Boolean> finalDecisionPhaseStrategy = (answers)->{

            System.out.println("final decision phase");

            for (TriggerAnswer answer:answers.values()){
                if (!answer.isDecision()){
                    event.setMessage("отказ в удалении файла. Не получено одобрение других участников");
                    event.getInternalData().setCompensationPhase(true);
                    return false;
                }
            }


            return true;
        };



        PhaseStrategy strategy = PhaseStrategy.constructStrategy()
                .addPhase(activityPollingPhaseStrategy, 500)
                .addPhase(finalDecisionPhaseStrategy, 5000)
                .getStrategy();

        PhaseTrigger phaseTrigger = PhaseTrigger.builder()
                .phaseStrategy(strategy)
                .onFeedStrategy(onFeedStrategy)
                .needPollingMessage(true)
                .correlationId(event.getContext().getCorrelationId())
                .message(event.getContext().getUsername()+" собирается удалить файл "+event.getExternalData()
                        .getName()+"."+event.getExternalData().getExtension())
                .triggerExternalData(new SimpleTriggerData(TriggerType.YES_OR_NOT, Map.of("fileId",
                        event.getExternalData().getFileId().toString())))
                .build();


        // чтобы система увидела триггер, следующий шаг обязательно должен сопровождаться аннотацией waiting for
        createTrigger(phaseTrigger);





    }

    /*
    права на выполнение операции проверяются в верхнем уровне
     */

    @Step(name = "blockFile")
    @WaitingFor(time = 20)
    @Next(name = "removeFileFromDb")
    public void blockFile(FileRemovalEvent event){

       transaction().execute(status -> {
            Optional<File> fileCheck = fileRepository.findByIdForUpdate(event.getExternalData().getFileId());
            if (fileCheck.isEmpty()){
                throw new IllegalStateException("файла не существует");
            }
            File file = fileCheck.get();
            if (file.getStatus()!= FileStatus.AVAILABLE){
                throw new IllegalStateException("файл не доступен для удаления - занят другим процессом");
            }

            file.setStatus(FileStatus.REMOVING);




            return null;
        });







    }

    @Step(name = "removeFileFromDb")
    @Next(name = "removeFileFromDisk")
    public void removeFileFromDb(FileRemovalEvent event){



        transaction().execute(status -> {
            try {
                fileRepository.deleteById(event.getExternalData().getFileId());
            }
            catch (Exception e){
                throw new IllegalStateException("Ошибка удаления файла. Причина: "+e.getMessage());
            }

            return null;
        });


    }

    @EndingStep(name = "removeFileFromDisk")
    @MaxRetry(maxCount = 3)
    public void removeFileFromDisk(FileRemovalEvent event){


        try {
            Files.delete(Path.of(event.getInternalData().getFilePath()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("ошибка удаления файла с диска");
        }


    }





    @Override
    public void compensationStrategy(FileRemovalEvent event) {
        System.out.println("compensation for file removal phase = > "+event.getInternalData().getCurrentStep());
    }
}
