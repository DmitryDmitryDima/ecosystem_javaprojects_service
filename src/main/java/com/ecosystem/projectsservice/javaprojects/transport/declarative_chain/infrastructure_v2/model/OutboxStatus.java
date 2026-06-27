package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model;

public enum OutboxStatus {
    PROCESSING, // прочитано, находится в процессе обработки

    PROCESSED, // обработано с соответствующим callback
    WAITING, // ожидает прочтения
    WAITING_FOR_EXTERNAL // ожидает внешнего сигнала (к примеру - триггера)
}
