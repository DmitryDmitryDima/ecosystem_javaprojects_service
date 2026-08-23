package com.ecosystem.projectsservice.javaprojects.external_messaging.message;



public enum MessageStatus {


    ERROR, // сообщение об ошибке (завершение)
    PROCESSING, // в процессе
    POLLING, // опрос
    SUCCESS // успешное завершение
}
