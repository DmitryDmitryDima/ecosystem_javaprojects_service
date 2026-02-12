package com.ecosystem.projectsservice.javaprojects.processes.external_events.data.triggers;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.TriggerType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class SimpleTriggerData implements ExternalEventData {

    // помогает ui понять, какой тип отображения необходим для триггера, хотя по факту это не обязательно,
    // ui должен быть настроен под конкретный процесс
    private TriggerType type;

    // дабы не плодить кучу наследников, инфу можно хранить тут, тем самым давая ui свободу интерпретировать все самостоятельно
    private Map<String, String> triggerInfo;


}
