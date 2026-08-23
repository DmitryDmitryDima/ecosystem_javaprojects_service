package com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.chain.structure.DeclarativeChainSpringAdapter;
import com.ecosystem.projectsservice.javaprojects.external_messaging.context.ExternalContext;
import com.ecosystem.projectsservice.javaprojects.external_messaging.data.ExternalData;

// должна быть задана категория внешнего сообщения и строковый тип ивентов (аннотацией)
public abstract class BroadcastableChain <E extends ExternallyConnectedChainEvent <? extends ExternalContext,
        ? extends ExternalData>>
        extends DeclarativeChainSpringAdapter <E> {



}
