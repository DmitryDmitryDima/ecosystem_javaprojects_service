package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// триггер, ожидающий действий от пользователей проекта
// при первоначальном опросе учитывается информация о file id для актуализации рендеров

@Getter
@Setter
@SuperBuilder
public class SimpleUserControlledProjectTrigger extends Trigger {

    private Long projectId;

    // if null - all members inside project should receive trigger message
    private Long fileId;

    // Мы должны получить какой-либо ответ (да или нет) от всех участников опроса
    // на основе стратегии мы принимаем решение (при получении каждого из ответов мы опираемся на соотношение true/false)
    private ConcurrentHashMap<UUID, Boolean> opinions;


    @Override
    public TriggerType getTriggerType() {
        return TriggerType.SIMPLE_USER_CONTROLLED_PROJECT_TRIGGER;
    }
}
