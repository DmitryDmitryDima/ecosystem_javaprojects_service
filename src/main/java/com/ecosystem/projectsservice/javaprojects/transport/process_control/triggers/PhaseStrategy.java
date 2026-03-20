package com.ecosystem.projectsservice.javaprojects.transport.process_control.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PhaseStrategy {

    private List<Phase> actions;

    private PhaseStrategy(List<Phase> actions){
        this.actions = actions;
    }

    public List<Phase> getActions() {
        return actions;
    }






    public boolean isLast(long index){
        return index==actions.size()-1;
    }

    public static PhaseStrategyBuilder constructStrategy(){
        return new PhaseStrategyBuilder();
    }

    public static class PhaseStrategyBuilder{
        private List<Phase> predefinedActions = new ArrayList<>();

        public PhaseStrategy getStrategy(){
            return new PhaseStrategy(predefinedActions);
        }

        public PhaseStrategyBuilder addPhase(Function<Map<String, TriggerAnswer>, Boolean> action, long period){
            predefinedActions.add(new Phase(action, period));
            return this;
        }



    }


    public static class Phase{
        private long period;
        private Function<Map<String, TriggerAnswer>, Boolean> action;

        public Phase(Function<Map<String, TriggerAnswer>, Boolean> action, long period) {
            this.period = period;
            this.action = action;
        }

        public long getPeriod() {
            return period;
        }

        public void setPeriod(long period) {
            this.period = period;
        }

        public Function<Map<String, TriggerAnswer>, Boolean> getAction() {
            return action;
        }

        public void setAction(Function<Map<String, TriggerAnswer>, Boolean> action) {
            this.action = action;
        }
    }

}
