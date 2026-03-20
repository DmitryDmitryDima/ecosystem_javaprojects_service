package com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.function.Function;

/*
данный триггер полагается на периодическую проверку ответов - суть проверки и их количество можно задать в стратегиях
таким образом мы концептуально отделяем waiting for время от времени внутри триггера, так как последнее всегда задается напрямую
 */
@Data
@SuperBuilder
public class PhaseTrigger extends Trigger{

    private PhaseStrategy phaseStrategy;

    // если onFeedStrategy возвращает true, то это можно считать сигналом к остановке триггера, если false - просто выполнение и игнор
    // onFeedStrategy может отсутствовать, оставляя триггер полностью фазовым
    private Function<Map<String, TriggerAnswer>, Boolean> onFeedStrategy;


    public boolean hasOnFeedStrategy(){return onFeedStrategy!=null;}







}
