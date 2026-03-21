package com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers;

import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;

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
