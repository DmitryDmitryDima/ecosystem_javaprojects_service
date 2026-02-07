package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.testing;

import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.exceptions.StepInterruptedException;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.triggers.SimpleTriggerData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.filesave.FileSaveEvent;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.ChainProcess;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.CustomTrigger;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.TriggerAnswer;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.TriggerType;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@ExternalResultType(event = ExternalEventType.JAVA_PROJECT_FILE_SAVE)
public class ControlTestChain extends ControlledOutboxChain<FileSaveEvent> {
    @Override
    protected ExternalEvent<? extends ExternalEventContext> bindResultingEvent() {
        return new ProjectEventFromUser();
    }

    @Override
    protected void setProcessAssociations(FileSaveEvent event) {

    }

    @Override
    @Async("taskExecutor")
    @EventListener
    public void catchEvent(FileSaveEvent event) {
        super.processEvent(event);
    }

    @Override
    public void compensationStrategy(FileSaveEvent event) {
        System.out.println("trigger compensation strategy");
    }


    @OpeningStep(name="openingStep")
    @Next(name="middleStepOne")
    public void openingStep(FileSaveEvent fileSaveEvent){


        System.out.println("opening step");

    }

    @Step(name="middleStepOne")
    @Next(name="middleStepTwo")
    public void middleStepOne(FileSaveEvent fileSaveEvent){

        createTrigger(

                CustomTrigger.builder()
                        .message("Выполнять ли действие 2?")
                        .decisionPhaseWaitingTimeInMs(20*1000)
                        .correlationId(fileSaveEvent.getContext().getCorrelationId())
                        .triggerExternalData(new SimpleTriggerData(TriggerType.YES_OR_NOT, new HashMap<>()))
                        .activityPhaseApprovalStrategy(
                                (answers)->{

                                    System.out.println("activity check");

                                    for (TriggerAnswer answer:answers.values()){
                                        if (answer.getContent().equals("no")){
                                            return false;
                                        }
                                    }
                                    return true;
                                }
                        )
                        .decisionPhaseApprovalStrategy((answers)->{

                            System.out.println("decision check");
                            fileSaveEvent.setMessage("Не получено одобрение для переходу к шагу 2");
                            fileSaveEvent.getInternalData().setCompensationPhase(true);

                            return false;
                        })

                        .build()
        );


    }

    @Step(name="middleStepTwo")
    @Next(name="endingStep")
    @MaxDuration(timeInSec = 60)
    @WaitingFor(timeInSec = 60)
    public void middleStepTwo(FileSaveEvent fileSaveEvent){
        System.out.println("middle step two");


        ChainProcess chainProcess = getProcessState(fileSaveEvent.getContext().getCorrelationId());

        while (chainProcess.getStatus().get()== ChainProcess.ProcessStatus.RUNNING && !Thread.currentThread().isInterrupted()){
            try {
                Thread.sleep(3000);
                System.out.println("long task in middle step two");
            }
            catch (InterruptedException interruptedException){
                throw new StepInterruptedException("middle two was stopped");
            }
        }



        if(chainProcess.getStatus().get()== ChainProcess.ProcessStatus.STOPPED){
            throw new StepInterruptedException("middle two was stopped");
        }






    }





    @EndingStep(name = "endingStep")
    public void endingStep(FileSaveEvent fileSaveEvent){
        System.out.println("ending step");

    }



}
