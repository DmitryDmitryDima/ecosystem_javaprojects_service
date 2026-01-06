package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events;

import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.ProjectExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.to_external_queue.UserExternalEventContext;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/*
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "serialized_as")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserExternalEventContext.class, name = "UserExternalEventContext"),
        @JsonSubTypes.Type(value = ProjectExternalEventContext.class, name = "ProjectExternalEventContext")
})

 */
public interface ExternalEventContext {

}
