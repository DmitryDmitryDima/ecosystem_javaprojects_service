package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure;

import java.util.UUID;


public class ControlledOutboxDeclarativeChain {



    // регистрация нативного cmd процесса - обязательно для ситуаций, когда таковые имеются в шаге
    public void registerProcess(UUID correlationId, Process process){

    }
}
