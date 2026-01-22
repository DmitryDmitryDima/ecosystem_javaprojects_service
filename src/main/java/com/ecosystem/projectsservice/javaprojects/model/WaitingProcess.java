package com.ecosystem.projectsservice.javaprojects.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;
import java.util.UUID;

/*
данный объект генерируется для ситуаций, когда задан waiting for
Предназначен для ситуаций, когда между запросом и внешним ответом происходит падение JVM
Если нет ни state объекта, ни waiting объекта, то ивент игнорируется
Если юзер останавливает цепочку - это удаляет waiting объекты
 */
@Entity
public class WaitingProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant waitingFor;

    private UUID correlationId;






}
