package com.ecosystem.projectsservice.javaprojects.transport.external_events.event_categories;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.events.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.UserPersonalEventContext;

// ивент, автором которого является юзер, слушается им же
@EventQualifier("user_personal_event")
public class UserPersonalEvent extends ExternalEvent<UserPersonalEventContext> {
}
