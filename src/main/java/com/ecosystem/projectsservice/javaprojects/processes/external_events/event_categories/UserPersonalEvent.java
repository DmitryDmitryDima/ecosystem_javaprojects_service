package com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.UserPersonalEventContext;

// ивент, автором которого является юзер, слушается им же
@EventQualifier("user_personal_event")
public class UserPersonalEvent extends ExternalEvent<UserPersonalEventContext> {
}
