package com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.external_messaging.context.ExternalContext;
import com.ecosystem.projectsservice.javaprojects.external_messaging.data.ExternalData;
import lombok.Getter;
import lombok.Setter;

// ивент для модифицированной цепочки, несущий в себе внешний контекст и внешние данные
// для сборки во внешнее сообщение



@Getter
@Setter
public abstract class ExternallyConnectedChainEvent <C extends ExternalContext,
        D extends ExternalData>
        extends ChainEvent {


    private C externalContext;

    private D externalData;
}
