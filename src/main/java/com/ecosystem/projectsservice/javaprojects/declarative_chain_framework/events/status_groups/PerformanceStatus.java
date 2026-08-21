package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events.status_groups;


// статус выполнения процесса
// по идее компенсация никогда не выполняется сразу
public enum PerformanceStatus {

    STOPPED_BEFORE_STEP, STOPPED_DURING_STEP, STOPPED_AFTER_STEP,

    CRASHED, STEP_PERFORMED, STEP_RETRY, CHAIN_INITIATED
}
