package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control;

public enum ProcessAvatarStatus {

    WAITING, // ожидание следующего шага

    RUNNING, // шаг
    STOPPED, // процесс остановлен
    TERMINATED, // процесс остановлен, очищен и может быть выброшен из всех хранилищ


}
