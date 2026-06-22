package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations;

public enum ChainTimeUnit {

    MS, SEC, MIN, HOURS, DAYS,

    // наличие everlasting означает, что время не смотрим - шаг вечен - он живет, пока есть runtime
    // TODO ДОБАВЬ EVERLASTING
}
