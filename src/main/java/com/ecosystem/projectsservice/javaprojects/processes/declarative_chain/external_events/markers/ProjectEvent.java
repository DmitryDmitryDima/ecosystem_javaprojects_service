package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.markers;


import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ProjectExternalEventContext;
import lombok.experimental.SuperBuilder;


@EventQualifier("project_event")
public class ProjectEvent extends ExternalEvent<ProjectExternalEventContext> {

}
