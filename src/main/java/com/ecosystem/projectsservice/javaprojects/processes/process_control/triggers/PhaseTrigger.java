package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

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







}
