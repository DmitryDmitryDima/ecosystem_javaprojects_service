package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger;

import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.PhaseStrategy;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers.TriggerAnswer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TriggerPhaseStrategy {


    private List<TriggerPhase> actions;

    private TriggerPhaseStrategy(List<TriggerPhase> actions){
        this.actions = actions;
    }

    public List<TriggerPhase> getActions() {
        return actions;
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
