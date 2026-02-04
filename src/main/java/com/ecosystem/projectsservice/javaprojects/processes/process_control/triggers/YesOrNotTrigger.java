package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.InternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.triggers.TriggerDataEnvelope;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class YesOrNotTrigger extends Trigger {

    private ConcurrentHashMap<UUID, BooleanAnswer> opinions = new ConcurrentHashMap<>();

    public YesOrNotTrigger(UUID correlationid, String message) {
        super(correlationid, message);
    }

    @Override
    public void consumeAnswer(UserTriggerAnswer answer) {
        if (answer instanceof BooleanAnswer booleanAnswer){
            opinions.put(booleanAnswer.getFrom(), booleanAnswer);
        }
    }






    // можно добавить стратегию
    @Override
    public boolean hasApproval() {



        for (BooleanAnswer answer:opinions.values()){
            // если null, значит пользователь получил ивент и сигнализировал о том, что реакция ожидается
            if (answer==null || !answer.getAnswer()){
                return false;
            }
        }


        return true;
    }

    @Override
    public TriggerDataEnvelope getTriggerEnvelope() {
        TriggerDataEnvelope triggerDataEnvelope = new TriggerDataEnvelope();
        triggerDataEnvelope.setTriggerType(TriggerType.YES_OR_NOT);
        return triggerDataEnvelope;
    }

    @Override
    public void onApprove(InternalEventData internalEventData) {

    }


}
