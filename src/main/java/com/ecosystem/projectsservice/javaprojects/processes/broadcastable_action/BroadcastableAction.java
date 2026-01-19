package com.ecosystem.projectsservice.javaprojects.processes.broadcastable_action;

import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.EventStatus;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;





// данный сервис позволяет обернуть действие, совершаемое за один шаг, и при этом генерирующее ивент для внешних систем
@Service
public class BroadcastableAction {

    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private ObjectMapper mapper;



    public StepBuilder createAction(Supplier<ActionResult<? extends ExternalEventContext,? extends ExternalEventData>> action){

        return new StepBuilder(action);
    }

    public Context statelessAction(Runnable action){
        return new StatelessStepBuilder(action);
    }



    public interface Context{
        Data withContext(Supplier<? extends ExternalEventContext> context);
    }

    public interface Data{
        Event withData(Supplier<? extends ExternalEventData> data);
    }

    public interface Event{
        Type withEvent(Supplier<ExternalEvent<? extends ExternalEventContext>> event);
    }

    public interface Type {
        Message withType(ExternalEventType externalEventType);
    }

    public interface Message {
        Execute withMessage(String message);
    }

    public interface Execute{
        void execute() throws ActionExecutionException;
    }













    // используется в случае, если действие не вносит какие-либо изменения во входящие данные
    private class StatelessStepBuilder implements Context, Data, Event, Type, Message, Execute{
        private Runnable action;
        private ExternalEventType externalEventType;
        private Supplier<? extends ExternalEventContext> context;
        private Supplier<? extends ExternalEventData> data;
        private String message;

        private Supplier<ExternalEvent<? extends ExternalEventContext>> externalEvent;




        public StatelessStepBuilder(Runnable action){
            this.action = action;
        }


        @Override
        public Data withContext(Supplier<? extends ExternalEventContext> context) {
            this.context = context;
            return this;
        }

        @Override
        public Event withData(Supplier<? extends ExternalEventData> data) {
            this.data = data;
            return this;
        }

        @Override
        public Type withEvent(Supplier<ExternalEvent<? extends ExternalEventContext>> event) {
            this.externalEvent = event;
            return this;
        }

        @Override
        public void execute() throws ActionExecutionException {
            BroadcastableAction.this.execute(this);
        }

        @Override
        public Execute withMessage(String message) {
            this.message = message;
            return this;
        }

        @Override
        public Message withType(ExternalEventType externalEventType) {
            this.externalEventType = externalEventType;
            return this;
        }
    }






    public class StepBuilder{
        private Supplier<ActionResult<? extends ExternalEventContext,? extends ExternalEventData>> action;
        private Runnable onError;
        private ExternalEventType externalEventType;
        private ExternalEvent<? extends ExternalEventContext> externalEvent;
        private boolean needErrorMessage;

        private StepBuilder(Supplier<ActionResult<? extends ExternalEventContext,? extends ExternalEventData>> action){
            this.action = action;
        }

        // компенсация, если она вдруг нужна
        public StepBuilder onErrorAction(Runnable onError){
            this.onError = onError;
            return this;
        }

        public StepBuilder needErrorBroadcast(){
            this.needErrorMessage = true;
            return this;
        }

        // имя ивента
        public StepBuilder withExternalEventType(ExternalEventType name){
            this.externalEventType = name;
            return this;
        }

        // тип ивента
        public StepBuilder withExternalEventCategory(ExternalEvent<? extends ExternalEventContext> event){
            this.externalEvent = event;
            return this;
        }

        public void execute() throws Exception {
            BroadcastableAction.this.execute(this);
        }


    }

    private void execute(StatelessStepBuilder statelessStepBuilder) throws ActionExecutionException {
        try {
            statelessStepBuilder.action.run();

            ExternalEvent externalEvent = statelessStepBuilder.externalEvent.get();

            externalEvent.setMessage(statelessStepBuilder.message);
            externalEvent.setStatus(EventStatus.SUCCESS);
            externalEvent.setContext(statelessStepBuilder.context.get());
            externalEvent.setType(statelessStepBuilder.externalEventType.getName());

            externalEvent.setData(mapper.writeValueAsString(statelessStepBuilder.data.get()));



            publisher.publishEvent(externalEvent);

        }
        catch (Exception e){




            // todo onError
            throw new ActionExecutionException("ошибка выполнения действия. Причина "+e.getCause().getMessage());
        }
    }




    private void execute(StepBuilder stepBuilder) throws Exception{
        try {
            ActionResult<? extends ExternalEventContext,? extends ExternalEventData> result = stepBuilder.action.get();

            ExternalEvent externalEvent = stepBuilder.externalEvent;

            externalEvent.setMessage(result.getMessage());
            externalEvent.setStatus(EventStatus.SUCCESS);
            externalEvent.setContext(result.getContext());
            externalEvent.setType(stepBuilder.externalEventType.getName());
            System.out.println(result.getExternalData());
            externalEvent.setData(mapper.writeValueAsString(result.getExternalData()));


            System.out.println(externalEvent.getData()+" data");

            publisher.publishEvent(externalEvent);

        }
        catch (Exception e){

            if (stepBuilder.needErrorMessage){
                // todo рассылка сообщения об ошибке
            }

            if (stepBuilder.onError!=null){
                stepBuilder.onError.run();
            }


            // todo onError
            throw new ActionExecutionException("ошибка выполнения действия. Причина "+e.getCause().getMessage());
        }
    }










}
