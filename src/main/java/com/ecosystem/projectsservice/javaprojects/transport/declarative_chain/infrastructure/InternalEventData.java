package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public abstract class InternalEventData {

    private long currentRetry = 0;

    // текущий шаг - зная его, мы можем перейти к следующему
    private String currentStep; // if null - it starting step


    // данное поле вставляется в менеджере
    @JsonIgnore
    private UUID outboxParent = null;

    // явно указываем, что достигнуто и должно быть исполнено состояние компенсации
    private boolean compensationPhase = false;

    // todo - причина компенсации





}
