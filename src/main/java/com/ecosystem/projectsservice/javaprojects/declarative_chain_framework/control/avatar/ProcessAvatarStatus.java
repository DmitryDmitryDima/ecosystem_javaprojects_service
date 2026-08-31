package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.avatar;

public enum ProcessAvatarStatus {

    WAITING, // ожидание следующего шага

    RUNNING, // шаг

    COMPENSATING, // компенсация
    STOPPED, // процесс остановлен
    CRASHED, // crash с исчерпанием ретраев
    TERMINATED, // процесс остановлен, очищен и может быть выброшен из всех хранилищ


    OUTPUT_ERROR_AFTER_STEP, // провалилась публикация следующего шага и, соответственно, коллбэк для предыдущего

    OUTPUT_ERROR_AFTER_FINAL_STEP, // провалилась публикация финального шага

    OUTPUT_ERROR_AFTER_STOP, // провалилась публикация после того, как очередь была остановлена

    OUTPUT_ERROR_AFTER_CRASH, // провал публикации после crash

    OUTPUT_ERROR_AFTER_COMPENSATION





}
