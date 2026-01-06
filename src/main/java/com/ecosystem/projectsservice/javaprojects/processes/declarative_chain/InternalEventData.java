package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class InternalEventData {

    private long currentRetry;

    // текущий шаг - зная его, мы можем перейти к следующему
    private String currentStep; // if null - it starting step


    private long outboxParent = -1;

}
