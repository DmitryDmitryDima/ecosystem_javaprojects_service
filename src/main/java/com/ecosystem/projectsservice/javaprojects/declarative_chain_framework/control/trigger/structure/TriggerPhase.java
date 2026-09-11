package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.structure;

import java.util.List;
import java.util.Map;
import java.util.function.Function;



// период задается относительно предыдущей фазы
public class TriggerPhase {

    private long msDelay;
    private Function<Map<String, List<TriggerFeed>>, Boolean> action;

    private String phaseLog;

    private Exception exception;

    public TriggerPhase(Function<Map<String, List<TriggerFeed>>, Boolean> action,
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

    public Function<Map<String, List<TriggerFeed>>, Boolean> getAction() {
        return action;
    }

    public void setAction(Function<Map<String, List<TriggerFeed>>, Boolean> action) {
        this.action = action;
    }


    public String getPhaseLog() {
        return phaseLog;
    }

    public void setPhaseLog(String phaseLog) {
        this.phaseLog = phaseLog;
    }

    public Exception getResultingException() {
        return exception;
    }

    public void setResultingException(Exception exception) {
        this.exception = exception;
    }
}
