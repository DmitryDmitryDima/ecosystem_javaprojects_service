package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger;


import java.util.Map;
import java.util.function.Function;

public class ReactiveChainTrigger extends ChainTrigger{



    private Function<Map<String, TriggerFeed>, Boolean> onFeedReaction;




    // true означает сигнал о том, что процесс должен двигаться дальше
    public synchronized boolean react(TriggerFeed feed){


        super.react(feed);

        if (onFeedReaction == null) return false; // стратегии может не быть,
        // например в классе наследнике - фазовом триггере

        boolean reaction = onFeedReaction.apply(getAllFeeds());

        if (reaction){
            deactivate();
        }

        return reaction;


    }


    public Function<Map<String, TriggerFeed>, Boolean> getOnFeedReaction() {
        return onFeedReaction;
    }

    public void setOnFeedReaction(Function<Map<String, TriggerFeed>, Boolean> onFeedReaction) {
        this.onFeedReaction = onFeedReaction;
    }
}
