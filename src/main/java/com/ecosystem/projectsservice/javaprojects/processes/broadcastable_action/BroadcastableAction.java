package com.ecosystem.projectsservice.javaprojects.processes.broadcastable_action;

import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventName;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.EventStatus;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventContext;
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






    public class StepBuilder{
        private Supplier<ActionResult<? extends ExternalEventContext,? extends ExternalEventData>> action;
        private Runnable onError;
        private ExternalEventName externalEventName;
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
        public StepBuilder withExternalName(ExternalEventName name){
            this.externalEventName = name;
            return this;
        }

        // тип ивента
        public StepBuilder withExternalEvent(ExternalEvent<? extends ExternalEventContext> event){
            this.externalEvent = event;
            return this;
        }

        public void execute() throws Exception {
            BroadcastableAction.this.execute(this);
        }


    }

    private void execute(StepBuilder stepBuilder) throws Exception{
        try {
            ActionResult<? extends ExternalEventContext,? extends ExternalEventData> result = stepBuilder.action.get();

            ExternalEvent externalEvent = stepBuilder.externalEvent;

            externalEvent.setMessage(result.getMessage());
            externalEvent.setStatus(EventStatus.SUCCESS);
            externalEvent.setContext(result.getContext());
            externalEvent.setType(stepBuilder.externalEventName.getName());
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
