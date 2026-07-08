package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.status_groups;



// статус доставки
public enum DeliveryStatus {


    SUCCESS_READING,
    EXPIRED_READING,
    EXPIRED_PROCESSING_WITH_CONTEXT, // выполнение просрочено, но аватар жив
    EXPIRED_PROCESSING_NO_CONTEXT,
    EXPIRED_WAITING // не дождались внешнего сигнала
}
