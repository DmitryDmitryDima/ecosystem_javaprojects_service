package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.compensation;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events.ChainEvent;


// интерфейс compensator можно адаптировать под все существующие сочетания ошибок,
// сделав их настройку более удобной
public interface Compensator <E extends ChainEvent> {

    void compensate(E event);
}
