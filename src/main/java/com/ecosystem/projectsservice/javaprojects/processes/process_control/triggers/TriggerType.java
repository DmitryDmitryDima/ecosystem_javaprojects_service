package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

// тип триггера ориентирован на то, что ожидается для него в качестве ответа. Опционален, так как ответ - string
public enum TriggerType {
    // ДА ИЛИ НЕТ, ВЫБОР ИЗ ПЕРЕЧНЯ ВАРИАНТОВ, ТЕКСТОВЫЙ КОНТЕНТ
    YES_OR_NOT, CHOICE, TEXT_CONTENT
}
