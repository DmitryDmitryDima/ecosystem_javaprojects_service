package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChainOutput {



    private ChainEvent event;




    private OutboxEvent.OutboxEventStatus status;
    // последняя смена статуса
    private Instant last_update;

    // время, до которого ивент должен быть прочитан (комбинация с waiting for или waiting)
    private Instant readExpiration;

    // время, за которое ивент должен получить состояние processed - милисекунды
    private Long performanceExpirationPeriod;

}
