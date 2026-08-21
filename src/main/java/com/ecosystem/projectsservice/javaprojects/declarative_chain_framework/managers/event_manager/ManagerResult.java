package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.event_manager;

public class ManagerResult {


    private boolean needDeadLetter;

    private boolean withCompensation;

    private Exception exception; // если есть, то была ошибка


    public static ManagerResult exception(Exception e){
        ManagerResult managerResult = new ManagerResult();

        managerResult.setException(e);

        return managerResult;
    }


    public static ManagerResult compensation(){
        ManagerResult managerResult = new ManagerResult();

        managerResult.setWithCompensation(true);

        return managerResult;

    }


    public static ManagerResult deadLetter(){
        ManagerResult managerResult = new ManagerResult();
        managerResult.setNeedDeadLetter(true);

        return managerResult;
    }


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
