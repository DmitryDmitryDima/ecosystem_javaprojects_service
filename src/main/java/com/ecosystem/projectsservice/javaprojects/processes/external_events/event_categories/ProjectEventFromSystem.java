package com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ProjectEventFromSystemContext;

// автор ивента - система (к примеру - запущенный проект), попадает в комнату проекта
@EventQualifier("project_event_from_system")
public class ProjectEventFromSystem extends ExternalEvent<ProjectEventFromSystemContext> {

}
