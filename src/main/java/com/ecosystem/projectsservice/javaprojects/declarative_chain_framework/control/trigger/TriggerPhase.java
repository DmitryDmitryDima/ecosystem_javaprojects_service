package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger;

import java.util.Map;
import java.util.function.Function;



// период задается относительно предыдущей фазы
public class TriggerPhase {

    private long msDelay;
    private Function<Map<String, TriggerFeed>, Boolean> action;

    public TriggerPhase(Function<Map<String, TriggerFeed>, Boolean> action,
                        long msDelay) {
        this.msDelay = msDelay;
        this.action = action;
    }

    public long getMsDelay() {
        return msDelay;
    }

    public void setMsDelay(long msDelay) {
        this.msDelay = msDelay;
    }

    public Function<Map<String, TriggerFeed>, Boolean> getAction() {
        return action;
    }

    public void setAction(Function<Map<String, TriggerFeed>, Boolean> action) {
        this.action = action;
    }
}
