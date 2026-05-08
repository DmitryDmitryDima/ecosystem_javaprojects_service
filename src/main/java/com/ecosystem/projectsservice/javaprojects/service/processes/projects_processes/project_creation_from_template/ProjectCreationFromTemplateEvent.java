package com.ecosystem.projectsservice.javaprojects.service.processes.projects_processes.project_creation_from_template;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.context_categories.UserPersonalEventContext;
import lombok.Setter;
import lombok.ToString;

@EventQualifier("project_creation_from_template")
@ToString
@Setter
public class ProjectCreationFromTemplateEvent extends DeclarativeChainEvent<
        UserPersonalEventContext,
        ProjectCreationFromTemplateExternalData,
        ProjectCreationFromTemplateInternalData
        >
{


}
