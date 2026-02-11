package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_removal;

import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.enums.StepTimeUnit;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.triggers.SimpleTriggerData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.*;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_FILE_REMOVAL)
public class FileRemovalChain extends ControlledOutboxChain<FileRemovalEvent> {

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

        PhaseStrategy strategy = PhaseStrategy.constructStrategy()
                .addPhase((answers)->{
                    System.out.println(answers);
                    System.out.println("activity poll");
                    return true;
                }, 500)

                .addPhase((answers)->{
                    System.out.println(answers);
                    System.out.println("middle phase");

                    // пример принятия решение на основе всех согласных

                    for (TriggerAnswer answer:answers.values()){
                        if (!answer.isDecision()){
                            event.getInternalData().setCompensationPhase(true);
                            return true;
                        }
                    }

                    return true;
                }, 5000)

                .addPhase((answers)->{
                    System.out.println(answers);
                    System.out.println("last phase");


                    return false;


                    },5000
                )


                .getStrategy();

        PhaseTrigger phaseTrigger = new PhaseTrigger(strategy);
        phaseTrigger.setCorrelationId(event.getContext().getCorrelationId());
        phaseTrigger.setMessage(event.getContext().getUsername()+" собирается удалить файл "+event.getExternalData()
                .getName()+"."+event.getExternalData().getExtension());

        phaseTrigger.setTriggerExternalData(new SimpleTriggerData(TriggerType.YES_OR_NOT, new HashMap<>()));

        createTrigger(phaseTrigger);





    }

    @Step(name = "blockFile")
    @WaitingFor(time = 20)
    @Next(name = "removeFile")
    public void blockFile(FileRemovalEvent event){

        Function<Map<String, TriggerAnswer>, Boolean> approvalStrategy = (answers)->{
            System.out.println(answers);

            return false;
        };

        ReactiveTrigger reactiveTrigger = new ReactiveTrigger(approvalStrategy);
        reactiveTrigger.setMessage("reactive trigger call you");
        reactiveTrigger.setCorrelationId(event.getContext().getCorrelationId());
        reactiveTrigger.setNeedPollingMessage(false);

        createTrigger(reactiveTrigger);
    }

    @EndingStep(name = "removeFile")
    @WaitingFor(time = 20)
    public void removeFile(FileRemovalEvent event){
        event.setMessage("Файл удален "+event.getExternalData().getName());

    }





    @Override
    public void compensationStrategy(FileRemovalEvent event) {
        System.out.println("compensation for file removal");
    }
}
