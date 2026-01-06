package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events;


import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox.FileSaveEventData;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/*
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "serialized_as")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FileSaveEventData.class, name = "FileSaveEventData"),

})

 */
public interface ExternalEventData {
}
