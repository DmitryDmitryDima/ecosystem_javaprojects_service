package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

public class TriggerPhaseStrategy {


    private List<TriggerPhase> actions;

    private List<Future<?>> activePhases = new ArrayList<>();

    private TriggerPhaseStrategy(List<TriggerPhase> actions){
        this.actions = actions;
    }

    public List<TriggerPhase> getActions() {
        return actions;
    }


    public void addActivePhase(Future<?> submittedPhase){

        activePhases.add(submittedPhase);
    }

    // посылаем cancel сигнал всем ожидающим фазам
    public void cancelPhases(){

        activePhases.forEach(phase->phase.cancel(true));
    }


    public static TriggerPhaseStrategyBuilder constructStrategy(){
        return new TriggerPhaseStrategy.TriggerPhaseStrategyBuilder();
    }


    public static class TriggerPhaseStrategyBuilder{
        private final List<TriggerPhase> predefinedActions = new ArrayList<>();

        public TriggerPhaseStrategy getStrategy(){
            return new TriggerPhaseStrategy(predefinedActions);
        }

        public TriggerPhaseStrategyBuilder addPhase(Function<Map<String, TriggerFeed>,
                                                            Boolean> action,
                                                    long period){

            // учитываем период предыдущей фазы
            long previousPeriod = predefinedActions
                    .isEmpty()?period:predefinedActions.getLast().getMsDelay();

            predefinedActions.add(new TriggerPhase(action, previousPeriod+period));

            return this;
        }



    }
}
