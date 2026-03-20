package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.ChainProcess;
import com.ecosystem.projectsservice.javaprojects.transport.process_control.ProcessAggregator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ChainManager {


    private final Map<String, Class<? extends DeclarativeChainEvent<? extends ExternalEventContext,
                ? extends ExternalEventData, ? extends InternalEventData>>> allInternalEvents = new HashMap<>();

    private final Map<String, Class<? extends ExternalEvent<? extends ExternalEventContext>>> allExternalEvents = new HashMap<>();

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ProcessAggregator aggregator;

    public void registerInternalEvent(String name, Class<? extends DeclarativeChainEvent<? extends ExternalEventContext,
            ? extends ExternalEventData, ? extends InternalEventData>> clazz){
        System.out.println(name+" registered");
        allInternalEvents.put(name, clazz);
    }

    public void registerExternalEvents(List<Class<? extends ExternalEvent<? extends ExternalEventContext>>> classes){
        for (Class<? extends ExternalEvent<? extends ExternalEventContext>> clazz:classes){
            EventQualifier annotation = clazz.getAnnotation(EventQualifier.class);
            if (annotation==null) throw new IllegalStateException("отсутствует аннотация @EventQualifier");
            allExternalEvents.put(annotation.value(), clazz);

        }
    }

    public void processExpiredWaitingForEvent(OutboxEvent outboxEvent){
        // мы должны спровоцировать компенсацию на текущем шаге
        try {
            if (allInternalEvents.containsKey(outboxEvent.getType())) {
                Class<? extends DeclarativeChainEvent<? extends ExternalEventContext,
                        ? extends ExternalEventData, ? extends InternalEventData>> clazz = allInternalEvents.get(outboxEvent.getType());
                DeclarativeChainEvent<? extends ExternalEventContext,
                        ? extends ExternalEventData, ? extends InternalEventData> deserializedEvent = mapper.readValue(outboxEvent.getPayload(), clazz);

                deserializedEvent.getInternalData().setOutboxParent(outboxEvent.getId()); // для callback

                deserializedEvent.setMessage("Превышено ожидание ответа "+deserializedEvent.getInternalData().getCurrentStep());
                deserializedEvent.getInternalData().setCompensationPhase(true);
                publisher.publishEvent(deserializedEvent);
            }
        }
        catch (Exception e){

        }
    }


    // todo пока что обрабатываем только зависшие шаги цепочек, без внимания к сообщениям
    public void processExpiredProcessingEvent(OutboxEvent outboxEvent){

        try {
            if (allInternalEvents.containsKey(outboxEvent.getType())){
                Class<? extends DeclarativeChainEvent<? extends ExternalEventContext,
                        ? extends ExternalEventData, ? extends InternalEventData>> clazz = allInternalEvents.get(outboxEvent.getType());
                DeclarativeChainEvent<? extends ExternalEventContext,
                        ? extends ExternalEventData, ? extends InternalEventData> deserializedEvent = mapper.readValue(outboxEvent.getPayload(), clazz);

                deserializedEvent.getInternalData().setOutboxParent(outboxEvent.getId()); // для callback

                ChainProcess chainProcess = aggregator.getChainProcessByCorrelationId(deserializedEvent.getContext().getCorrelationId());

                // если процесс существует - останавливаем его
                if (chainProcess!=null){
                    chainProcess.setExternalMessage("Время на выполнение истекло");
                    chainProcess.stop();
                }
                // процесса не существует - это означает состояние ошибки. Провоцируем компенсацию
                else {
                    deserializedEvent.getInternalData().setCompensationPhase(true);
                    deserializedEvent.setMessage("Время на выполнение истекло");
                    publisher.publishEvent(deserializedEvent);
                }




            }
        }

        catch (Exception e){

        }
    }

    // сценарий чтения waiting ивента с просроченным временем на чтение (допустим, если упала outbox бд)
    public void processExpiredWaitingEvents(OutboxEvent outboxEvent){
        try {
            if (allInternalEvents.containsKey(outboxEvent.getType())){
                Class<? extends DeclarativeChainEvent<? extends ExternalEventContext,
                        ? extends ExternalEventData, ? extends InternalEventData>> clazz = allInternalEvents.get(outboxEvent.getType());
                DeclarativeChainEvent<? extends ExternalEventContext,
                        ? extends ExternalEventData, ? extends InternalEventData> deserializedEvent = mapper.readValue(outboxEvent.getPayload(), clazz);

                deserializedEvent.getInternalData().setOutboxParent(outboxEvent.getId()); // для callback
                deserializedEvent.getInternalData().setCompensationPhase(true);
                deserializedEvent.setMessage("Ошибка системы - просрочено время чтения waiting ивента. Стадия:"+deserializedEvent.getMessage());
                publisher.publishEvent(deserializedEvent);

            }
        }
        catch (Exception e){

        }
    }

    // сценарий обычного чтения с переводом в processing
    public void processWaitingEvents(OutboxEvent outboxEvent){

        try {
            if (allInternalEvents.containsKey(outboxEvent.getType())){
                Class<? extends DeclarativeChainEvent<? extends ExternalEventContext,
                        ? extends ExternalEventData, ? extends InternalEventData>> clazz = allInternalEvents.get(outboxEvent.getType());
                DeclarativeChainEvent<? extends ExternalEventContext,
                        ? extends ExternalEventData, ? extends InternalEventData> deserializedEvent = mapper.readValue(outboxEvent.getPayload(), clazz);

                deserializedEvent.getInternalData().setOutboxParent(outboxEvent.getId()); // для callback
                publisher.publishEvent(deserializedEvent);

            }
            if (allExternalEvents.containsKey(outboxEvent.getType())){
                Class<? extends ExternalEvent<? extends ExternalEventContext>> clazz = allExternalEvents.get(outboxEvent.getType());


                ExternalEvent<? extends ExternalEventContext> deserializedEvent = mapper.readValue(outboxEvent.getPayload(), clazz);
                deserializedEvent.setOutboxParent(outboxEvent.getId());
                publisher.publishEvent(deserializedEvent);


            }
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("payload error "+e.getMessage());
        }


    }
}
