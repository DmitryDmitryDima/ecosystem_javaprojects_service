package com.ecosystem.projectsservice.javaprojects.processes.external_events.markers;


import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ProjectExternalEventContext;


@EventQualifier("project_event")
public class ProjectEvent extends ExternalEvent<ProjectExternalEventContext> {

}
