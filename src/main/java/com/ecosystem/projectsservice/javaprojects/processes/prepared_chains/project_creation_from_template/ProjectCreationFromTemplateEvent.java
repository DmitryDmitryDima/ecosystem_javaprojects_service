package com.ecosystem.projectsservice.javaprojects.processes.prepared_chains.project_creation_from_template;


import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.UserPersonalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.ProjectCreationFromTemplateExternalData;
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
