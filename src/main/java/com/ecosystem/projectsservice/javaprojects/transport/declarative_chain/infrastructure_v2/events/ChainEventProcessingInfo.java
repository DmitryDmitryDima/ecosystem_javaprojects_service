package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.status_groups.DeliveryStatus;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.status_groups.PerformanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChainEventProcessingInfo {

    // ПАРАМЕТРЫ ХОДА ПРОЦЕССА

    // время, когда последний шаг закончил выполнение
    // если произошел таймаут, что цепочка смотрит, можно ли делать ретрай
    private Instant previousStepExecutionTime;

    private long currentRetry = 0;

    // текущий шаг - зная его, мы можем перейти к следующему
    private String currentStep; // если null - это первый шаг




    // явно указываем, что достигнуто и должно быть исполнено состояние компенсации
    private boolean compensationPhase = false;


    // если true, то это означает, что при обработке
    // внутри цепи необходимо учитывать максимально допустимое время между ретраями
    private boolean stepTimeout = false;


    // в каком качестве был прочитан ивент
    private DeliveryStatus deliveryStatus;

    // что произошло с процессом в момент создания outbox ивента
    private PerformanceStatus performanceStatus;






    // todo будущеие поля при реализации циклов внутри процессов


    //
    // cycleCount // данное поле обновляется при шаге - цикле
}
