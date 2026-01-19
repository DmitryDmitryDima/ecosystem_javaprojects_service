package com.ecosystem.projectsservice.javaprojects.processes.broadcastable_action;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ActionResult <

        Context extends ExternalEventContext,
        External extends ExternalEventData

        >


{

    private Context context;

    private String message;

    private External externalData;

    public ActionResult(Context context, External externalData, String message){
        this.context = context;
        this.externalData = externalData;
        this.message = message;
    }


}
