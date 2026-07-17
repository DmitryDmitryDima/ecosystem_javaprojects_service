package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control;

public enum ProcessAvatarStatus {

    WAITING, // ожидание следующего шага

    RUNNING, // шаг
    STOPPED, // процесс остановлен
    CRASHED, // crash с исчерпанием ретраев
    TERMINATED, // процесс остановлен, очищен и может быть выброшен из всех хранилищ


    OUTPUT_ERROR, // провалилась публикация следующего шага и, соответственно, коллбэк для предыдущего


    OUTPUT_ERROR_AFTER_STOP, // провалилась публикация после того, как очередь была остановлена

    OUTPUT_ERROR_AFTER_CRASH // провал публикации после crash





}
