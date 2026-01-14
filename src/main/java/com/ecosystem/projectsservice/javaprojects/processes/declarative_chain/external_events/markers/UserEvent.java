package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.markers;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.UserExternalEventContext;

@EventQualifier("user_event")
public class UserEvent extends ExternalEvent<UserExternalEventContext> {
}
