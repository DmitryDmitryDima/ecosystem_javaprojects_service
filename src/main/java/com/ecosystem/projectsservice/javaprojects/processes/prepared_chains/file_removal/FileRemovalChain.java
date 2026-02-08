package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.file_removal;

import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.ControlledOutboxChain;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.triggers.SimpleTriggerData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.ProjectEventFromUser;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.CustomTrigger;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.TriggerType;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;

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
        createTrigger(CustomTrigger.builder()
                .message(event.getContext().getUsername()+" собирается удалить файл "+event.getExternalData()
                        .getName()+"."+event.getExternalData().getExtension())
                .correlationId(event.getContext().getCorrelationId())
                .triggerExternalData(new SimpleTriggerData(TriggerType.YES_OR_NOT, new HashMap<>()))
                .activityPhaseApprovalStrategy(
                        (answers)->{
                            System.out.println(answers);
                            return false;
                        }
                )
                .decisionPhaseApprovalStrategy(answers->{
                    //event.getInternalData().setCompensationPhase(true);
                    event.setMessage("removal fail");
                    return false;
                })

                .build());
    }

    @Step(name = "blockFile")
    @WaitingFor(timeInSec = 10)
    @Next(name = "removeFile")
    public void blockFile(FileRemovalEvent event){

    }

    @EndingStep(name = "removeFile")
    public void removeFile(FileRemovalEvent event){

    }





    @Override
    public void compensationStrategy(FileRemovalEvent event) {

    }
}
