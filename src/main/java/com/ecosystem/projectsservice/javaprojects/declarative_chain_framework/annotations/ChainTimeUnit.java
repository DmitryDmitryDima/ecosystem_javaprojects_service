package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations;

public enum ChainTimeUnit {

    MS, SEC, MIN, HOURS, DAYS,

    // наличие everlasting означает, что время не смотрим - шаг вечен - он живет, пока есть runtime
    // TODO ДОБАВЬ EVERLASTING
}
