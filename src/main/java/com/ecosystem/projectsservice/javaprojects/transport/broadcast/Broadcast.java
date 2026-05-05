package com.ecosystem.projectsservice.javaprojects.transport.broadcast;


import com.ecosystem.projectsservice.javaprojects.transport.external_events.EventStatus;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.function.Supplier;

@Service
public class Broadcast {



    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ObjectMapper objectMapper;


    // синхронные вычисления компонентов ивента
    public void sendSync(EventBuilder builder) throws BroadcastException {
        send(builder);
    }

    // полностью асинхронные вычисления
    @Async("virtualThreadFactory")
    public void sendAsync(EventBuilder builder) throws BroadcastException {
        send(builder);
    }

    private void send(EventBuilder builder) throws BroadcastException {

        try {
            ExternalEvent externalEvent = builder.externalEvent.get();

            externalEvent.setMessage(builder.message);
            externalEvent.setStatus(EventStatus.SUCCESS);
            externalEvent.setContext(builder.context.get());
            externalEvent.setType(builder.externalEventType.getName());

            externalEvent.setData(objectMapper.writeValueAsString(builder.data.get()));



            publisher.publishEvent(externalEvent);
        }
        catch (Exception e){
            throw new BroadcastException("Ошибка рассылки: "+e.getMessage());
        }
    }






    public static class EventBuilder {

        private ExternalEventType externalEventType;
        private Supplier<? extends ExternalEventContext> context;
        private Supplier<? extends ExternalEventData> data;
        private String message;

        private Supplier<ExternalEvent<? extends ExternalEventContext>> externalEvent;




        public EventContext useEvent(Supplier<ExternalEvent<? extends ExternalEventContext>> event){
            externalEvent = event;
            return new Constructor();
        }

        public interface EventContext{
            EventData withContext(Supplier<? extends ExternalEventContext> context);
        }

        public interface EventData{
            EventType withData(Supplier<? extends ExternalEventData> data);
        }



        public interface EventType {
            EventMessage withType(ExternalEventType externalEventType);
        }

        public interface EventMessage {
            Build withMessage(String message);
        }

        public interface Build {
            EventBuilder build();
        }




        private class Constructor implements EventContext, EventData, EventType, EventMessage, Build{



            @Override
            public EventData withContext(Supplier<? extends ExternalEventContext> context) {

                EventBuilder.this.context = context;

                return this;
            }

            @Override
            public EventType withData(Supplier<? extends ExternalEventData> data) {

                EventBuilder.this.data = data;

                return this;
            }

            @Override
            public Build withMessage(String message) {
                EventBuilder.this.message = message;
                return this;
            }

            @Override
            public EventMessage withType(ExternalEventType externalEventType) {
                EventBuilder.this.externalEventType = externalEventType;
                return this;
            }

            @Override
            public EventBuilder build() {
                return EventBuilder.this;
            }
        }
    }
}
