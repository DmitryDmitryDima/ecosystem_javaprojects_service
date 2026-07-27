package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.status_groups;


// статус выполнения процесса
// по идее компенсация никогда не выполняется сразу
public enum PerformanceStatus {

    STOPPED, CRASHED, STEP_PERFORMED, STEP_RETRY
}
