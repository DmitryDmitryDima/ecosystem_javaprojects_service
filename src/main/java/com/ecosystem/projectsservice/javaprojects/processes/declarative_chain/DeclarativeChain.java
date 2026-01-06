package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.ResultingName;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.repository.OutboxEventRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;

import java.lang.reflect.Method;
import java.util.Map;

public abstract class DeclarativeChain<E extends DeclarativeChainEvent<? extends ExternalEventContext>> {


    @Autowired
    private OutboxEventRepository outboxEventRepository;

    private String resultingEventName; // совпадает с именем state event'а



    @Autowired
    private ChainManager manager;

    private CachedMethod openingStep;

    private CachedMethod endingStep;



    private Map<String, CachedMethod> steps;

    // todo active process state cache associated by correlation id






    public abstract void configure() throws Exception;

    @PostConstruct
    public final void initiation() throws Exception{

        registerChainEvent();

        configure();

    }

    private void registerChainEvent() throws Exception{
        Class<E> chainEventClass = (Class<E>) GenericTypeResolver.resolveTypeArgument(getClass(), DeclarativeChain.class);

        //EventQualifier qualifier = chainEventClass.getAnnotation(EventQualifier.class);
        ResultingName qualifier = this.getClass().getAnnotation(ResultingName.class);
        if (qualifier==null) throw new IllegalStateException("missing qualifier name for chain event");
        this.resultingEventName = qualifier.name();
        manager.registerEvent(resultingEventName, chainEventClass);
    }










    // данный метод определяет, какой метод выполняется, базируясь на current step. При успешном выполнении current step проставляется на следующий
    protected final void processEvent(E event){

        // данный объект руководит состоянием ивента.
        InternalEventData internalEventData = event.getInternalEventData();

        try {

        }
        catch (Exception e){

        }

    }

    // механизм, позволяющей очереди ловить только свои ивенты
    public abstract void catchEvent(E event);



    private class CachedMethod{
        Method method;
        long maxRetry;
        String next;
        boolean message;
    }

    // сохраняем событие сообщения
    private void sendExternalResult(E internalEvent){
        ExternalEvent externalEvent = bindResultingEvent();
        externalEvent.setContext(internalEvent.getContext());
        externalEvent.setMessage(internalEvent.getMessage());
        externalEvent.setData(internalEvent.getExternalEventData());
        externalEvent.setType(resultingEventName);

    }

    // связываем очередь с одним из результирующих ивентов
    protected abstract ExternalEvent bindResultingEvent();



}
