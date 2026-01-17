package com.ecosystem.projectsservice.javaprojects.processes.external_events;

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
