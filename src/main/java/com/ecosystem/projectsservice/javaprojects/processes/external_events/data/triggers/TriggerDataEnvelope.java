package com.ecosystem.projectsservice.javaprojects.processes.external_events.data.triggers;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.TriggerType;
import lombok.Data;

import java.util.HashMap;

@Data
public class TriggerDataEnvelope implements ExternalEventData {

    private ExternalEventData data;
    private TriggerType triggerType;

    // тут также могут быть другие параметры, касающиеся триггера - в наследниках

}
