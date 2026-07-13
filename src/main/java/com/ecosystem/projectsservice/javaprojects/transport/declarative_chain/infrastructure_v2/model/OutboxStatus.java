package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model;

public enum OutboxStatus {
    PROCESSING, // прочитано, находится в процессе обработки

    PROCESSED, // обработано с соответствующим callback
    WAITING, // ожидает прочтения
    WAITING_FOR_EXTERNAL // ожидает внешнего сигнала (к примеру - триггера)


    ,

    MANAGER_CRASH, // ошибка обработки в менеджере
    // - ивент не достиг цепочки и не получил правильный коллбэк

    DEAD_LETTER // ошибка с ивентом требует ручного контроля или выделенного механизма

    // DEAD_LETTER И PROCESSED
    // - ИХ ПОЛУЧАЕТ КАЖДЫЙ ИВЕНТ В КОНЦЕ В ЗАВИСИМОСТИ ОТ ЖИЗНЕННОГО ЦИКЛА
}
