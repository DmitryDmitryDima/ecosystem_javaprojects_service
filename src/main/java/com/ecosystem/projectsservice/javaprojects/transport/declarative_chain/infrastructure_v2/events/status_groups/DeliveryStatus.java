package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.status_groups;



// статус доставки
public enum DeliveryStatus {


    SUCCESS_READING,
    OUTBOX_PROCESSOR_ERROR, // шаг завершился, но не смог опубликоваться в конце


    EXPIRED_READING,
    EXPIRED_PROCESSING_WITH_CONTEXT, // выполнение просрочено, но аватар жив
    EXPIRED_PROCESSING_MISSING_CONTEXT,
    EVERLASTING_STEP_MISSING_CONTEXT,
    EXPIRED_WAITING_FOR_SIGNAL // не дождались внешнего сигнала
}
