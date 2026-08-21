package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events.status_groups;



// статус доставки
public enum DeliveryStatus {


    SUCCESS_READING,
    OUTBOX_PROCESSOR_ERROR_AFTER_STEP, // шаг завершился, но не смог опубликоваться в конце
    OUTBOX_PROCESSOR_ERROR_AFTER_FINAL_STEP, // последний шаг завершился, но не смог опубликоваться
    OUTBOX_PROCESSOR_ERROR_AFTER_STOP, // ошибка публикации,
    // при этом на момент ошибки процесс был остановлен
    OUTBOX_PROCESSOR_ERROR_AFTER_CRASH, // ошибка публикации, шаг был остановлен




    EXPIRED_READING,
    EXPIRED_PROCESSING_WITH_CONTEXT, // выполнение просрочено, но аватар жив
    EXPIRED_PROCESSING_MISSING_CONTEXT,
    EVERLASTING_STEP_MISSING_CONTEXT,
    EXPIRED_WAITING_FOR_SIGNAL // не дождались внешнего сигнала
}
