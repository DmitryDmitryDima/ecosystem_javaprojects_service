package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager;

public class ManagerResult {


    private boolean needDeadLetter;

    private boolean withCompensation;

    private Exception exception; // если есть, то была ошибка


    public boolean isWithCompensation() {
        return withCompensation;
    }

    public void setWithCompensation(boolean withCompensation) {
        this.withCompensation = withCompensation;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public boolean isNeedDeadLetter() {
        return needDeadLetter;
    }

    public void setNeedDeadLetter(boolean needDeadLetter) {
        this.needDeadLetter = needDeadLetter;
    }
}
