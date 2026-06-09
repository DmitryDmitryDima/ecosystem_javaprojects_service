package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.enums;

// создаем кастомный enum, так как не все единицы времени нам нужны
public enum StepTimeUnit {

    MS, SEC, MIN, HOURS, DAYS,

    // наличие everlasting означает, что время не смотрим - шаг вечен - он живет, пока есть runtime
    // TODO ДОБАВЬ EVERLASTING

}
