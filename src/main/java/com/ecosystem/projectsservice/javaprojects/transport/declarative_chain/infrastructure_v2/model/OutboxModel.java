package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model;


import java.time.Instant;
import java.util.UUID;

public interface OutboxModel {


    // каждая outbox запись должна иметь уникальный идентификатор, отличный от process id
    UUID getOutboxUUID();

    // общий uuid процесса - генерируется до попадания в цепочку
    UUID getProcessUUID();

    // статус outbox записи
    OutboxStatus getStatus();

    // тип ивента - строковая форма. Необходим для расшифровки payload
    String getType();

    // payload - записанный chain step
    String getPayload();

    // время последней смены статуса
    Instant getLastUpdate();

    // время, до которого ивент должен быть прочитан
    Instant getReadExpiration();

    // максимальное время, в котором ивент может быть в статусе PROCESSING
    Long getPerformanceLimitTime();

    // счетчик прочтения ивента - обновляется всегда
    // необходим, например, при попытке проставить manager_crash status при обработке в reader
    Long getAllReadVersion();

    // счетчик processing прочтений - для специфичных сценариев зависания
    Long getAllReadProcessingVersion();


    // сообщение, для логов и ошибок

    String getMessage();

















}
