package com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories;


import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ProjectEventFromUserContext;


// ивент, автором которого является пользователь, предназначен для всех участников проекта (слушается комнатой проекта)
// контекст содержит в себе поле participants, и в зависимости от типа ивента предполагается,
// что некоторые ивенты будут направлены персонально участникам проекта
@EventQualifier("project_event_from_user")
public class ProjectEventFromUser extends ExternalEvent<ProjectEventFromUserContext> {

}
