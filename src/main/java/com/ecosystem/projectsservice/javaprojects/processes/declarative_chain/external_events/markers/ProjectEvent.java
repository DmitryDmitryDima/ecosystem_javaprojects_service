package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.markers;


import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEvent;
import lombok.experimental.SuperBuilder;


@EventQualifier("project_event")
public class ProjectEvent extends ExternalEvent {

}
