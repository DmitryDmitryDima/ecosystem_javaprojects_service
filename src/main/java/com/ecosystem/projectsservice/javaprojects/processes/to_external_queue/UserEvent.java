package com.ecosystem.projectsservice.javaprojects.processes.to_external_queue;


import com.ecosystem.projectsservice.javaprojects.processes.chains.EventName;
import com.ecosystem.projectsservice.javaprojects.processes.chains.Retryable;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Retryable(count = 3)
@EventName("outbox_user_event_for_external")
public class UserEvent extends BasicQueueEvent {


    private UserExternalEventContext context;

    private EventData eventData;






}
