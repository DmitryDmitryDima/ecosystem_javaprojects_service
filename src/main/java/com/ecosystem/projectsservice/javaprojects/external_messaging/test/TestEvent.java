package com.ecosystem.projectsservice.javaprojects.external_messaging.test;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.registry.ChainEventQualifier;
import com.ecosystem.projectsservice.javaprojects.external_messaging.context.context_category.ProjectEventFromSystemContextCategory;
import com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains.ExternallyConnectedChainEvent;


@ChainEventQualifier("broadcastable_chain_test")
public class TestEvent
        extends ExternallyConnectedChainEvent<ProjectEventFromSystemContextCategory,
        TestData> {






}
