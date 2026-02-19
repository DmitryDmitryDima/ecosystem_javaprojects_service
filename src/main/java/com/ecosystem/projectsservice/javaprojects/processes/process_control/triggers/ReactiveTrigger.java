package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;


import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.function.Function;

/*
каждая регистрация ответа предполагает проверку на завершенность операции
Таким образом закрытие триггера возможно сразу же при получении ответа
Данный триггер подходит для взаимодействия с одним юзером или для взаимодействия с внешними сообщениями

 */
@Data
@SuperBuilder
public class ReactiveTrigger extends Trigger {

    private Function<Map<String, TriggerAnswer>, Boolean> approvalStrategy;




    public boolean isApproved(){
        if (!isWaiting()) return false;
        return approvalStrategy.apply(getAnswers());
    }


}
