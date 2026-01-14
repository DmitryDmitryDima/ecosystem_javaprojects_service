package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.prepared_chains.project_creation_from_template;


import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.UserExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.prepared_chains.project_creation_from_template.event_structure.ProjectCreationFromTemplateExternalData;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.prepared_chains.project_creation_from_template.event_structure.ProjectCreationFromTemplateInternalData;
import lombok.Setter;
import lombok.ToString;

@EventQualifier("project_creation_from_template")
@ToString
@Setter
public class ProjectCreationFromTemplateEvent extends DeclarativeChainEvent<
        UserExternalEventContext,
        ProjectCreationFromTemplateExternalData,
        ProjectCreationFromTemplateInternalData
        >
{


}
