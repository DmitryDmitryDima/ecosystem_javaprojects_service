package com.ecosystem.projectsservice.javaprojects.external_messaging.modified_chains;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.chain.structure.DeclarativeChainSpringAdapter;
import com.ecosystem.projectsservice.javaprojects.external_messaging.context.ExternalContext;
import com.ecosystem.projectsservice.javaprojects.external_messaging.data.ExternalData;
import com.ecosystem.projectsservice.javaprojects.external_messaging.message.ExternalMessage;
import com.ecosystem.projectsservice.javaprojects.external_messaging.types.ExternalMessageType;

// должна быть задана категория внешнего сообщения и строковый тип ивентов (аннотацией)
public abstract class BroadcastableChain <E extends ExternallyConnectedChainEvent <? extends ExternalContext,
        ? extends ExternalData>>
        extends DeclarativeChainSpringAdapter <E> {



    private String messageType;






    protected abstract Class<? extends ExternalMessage> messageBind();


    @Override
    protected void readChainStructure() {




        ExternalMessageType messageTypeAnno
                = this.getClass().getAnnotation(ExternalMessageType.class);

        if (messageTypeAnno == null) throw new IllegalStateException("не указан тип внешнего сообщения");

        messageType = messageTypeAnno.type().getName();

        System.out.println(messageType +" message type");

        super.readChainStructure();
    }





}
