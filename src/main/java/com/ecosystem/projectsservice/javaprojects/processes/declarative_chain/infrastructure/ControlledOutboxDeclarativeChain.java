package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.MaxDuration;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.WaitingFor;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.exceptions.ChainInitiationException;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.EventStatus;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.ChainProcess;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.ProcessAggregator;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


// данная цепочка имеет функцию быть контролируемой со стороны пользователя/внешнего процесса
// существует возможность задавать параметры времени
// область использования - долгие процессы, которые можно поставить на паузу или остановить полностью (при этом необходимо прописать cleanup)
// также применяется для процессов, обращающихся к долгим микросервисам (ai) и допускающих, что ответ может либо не прийти, либо прийти, но с опозданием
// для реализации этого функционала вводится понятие state объекта, хранящегося в памяти и в некоторых случаях сопровождаемого waiting записью в бд
public abstract class ControlledOutboxDeclarativeChain <E extends DeclarativeChainEvent<? extends ExternalEventContext,
        ? extends ExternalEventData,
        ? extends InternalEventData>>  extends OutboxDeclarativeChain <E>  {

    // todo enforce unique step name for this feature
    private final Map<String, TimeControlParams> timeControlParamsMap = new HashMap<>();


    @Autowired
    private ProcessAggregator processAggregator;

    protected ProcessAggregator aggregator(){
        return processAggregator;
    }

    // регистрация нативного cmd процесса - обязательно для ситуаций, когда таковые имеются в шаге
    protected void registerNativeProcess(UUID correlationId, Process process){

    }

    // получение объекта ChainProcess для контроля метода
    protected ChainProcess getChainProcessState(UUID correlationId){
        return processAggregator.getChainProcessByCorrelationId(correlationId);
    }



    @Override
    protected void specificPreparation() {

        extractTimeControlParams();


    }

    protected void performCompensation(E event, ChainProcess chainProcess){


        try {
            compensationStrategy(event);

            ExternalEvent externalEvent = bindResultingEvent();
            externalEvent.setContext(event.getContext());
            externalEvent.setData(mapper().writeValueAsString(event.getExternalData()));
            externalEvent.setType(getResultingEventType().getName());
            externalEvent.setStatus(EventStatus.ERROR);
            externalEvent.setMessage(event.getMessage());

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setLast_update(Instant.now());
            outboxEvent.setType(externalEventQualifier);
            outboxEvent.setStatus(OutboxEvent.OutboxEventStatus.WAITING);
            outboxEvent.setPayload(mapper().writeValueAsString(externalEvent));

            // todo
            transaction().execute(status -> {
                outboxEventRepository.save(outboxEvent);
                return null;
            });

        }
        catch (Exception e){
            // todo специальная обработка исключения

        }



        finally {

            // STOPPED + NULL + NULL = КОМПЕНСАЦИЯ ОБРАБОТАНА
            chainProcess.getStatus().set(ChainProcess.ProcessStatus.STOPPED);
            transaction().execute(status -> {
                outboxCallback(event.getInternalData().getOutboxParent());
                return null;
            });

        }
    }


    @Override
    protected void processEvent(E event){

        // данный объект несет в себе персистентное состояние процесса
        InternalEventData internalEventData = event.getInternalData();

        // объект управления процессом
        ChainProcess chainProcess = aggregator().getChainProcessByCorrelationId(event.getContext().getCorrelationId());

        // метод, который будет выполняться следующим
        CachedMethod toExecute = resolveNextExecution(internalEventData);

        // сценарий компенсации
        if (toExecute==null){

            // RUNNING + NULL + NULL = КОМПЕНСАЦИЯ
            chainProcess.getWaitingForEvent().set(null); // ничего не ожидается - это означает компенсацию
            chainProcess.getCurrentStep().set(null); // ни один шаг цепочки не выполняется
            chainProcess.getStatus().set(ChainProcess.ProcessStatus.RUNNING);


            performCompensation(event, chainProcess);
        }



    }




    // если процесс необходимо ассоциировать не только с correlation id, но и с другими сущностями, необходимо внести функционал в этот метод
    public abstract void setProcessAssociations(E event);





    @Override
    public void init(E event) throws Exception {

        // создаем процесс
        ChainProcess chainProcess = new ChainProcess(event.getContext().getCorrelationId(),
                getResultingEventType(),
                getOpeningStep().name);

        try {



             aggregator().registerChainProcess(chainProcess);

             setProcessAssociations(event); // настройка ассоциаций
        }
        catch (Exception e){
            throw new ChainInitiationException("Ошибка создания state объекта");
        }
        super.init(event);
    }

    private void extractTimeControlParams(){

        // waitingFor аннотация не актуальна для шага открытия
        CachedMethod openingMethod = getOpeningStep();
        MaxDuration maxDurationOpening = openingMethod.method.getAnnotation(MaxDuration.class);

        TimeControlParams paramsForOpening = new TimeControlParams();
        paramsForOpening.duration = maxDurationOpening==null?-1L: maxDurationOpening.timeInSec();

        timeControlParamsMap.put(openingMethod.name, paramsForOpening);

        CachedMethod endingMethod = getEndingStep();
        MaxDuration maxDurationEnding = endingMethod.method.getAnnotation(MaxDuration.class);
        WaitingFor waitingForEnding = endingMethod.method.getAnnotation(WaitingFor.class);

        TimeControlParams paramsForEnding = new TimeControlParams();
        paramsForEnding.duration = maxDurationEnding==null?-1L: maxDurationEnding.timeInSec();
        paramsForEnding.waitingFor = waitingForEnding == null?-1L: waitingForEnding.timeInSec();

        timeControlParamsMap.put(endingMethod.name, paramsForEnding);

        for (CachedMethod step:getSteps().values()){
            MaxDuration maxDuration = step.method.getAnnotation(MaxDuration.class);
            WaitingFor waitingFor = step.method.getAnnotation(WaitingFor.class);

            TimeControlParams timeControlParams = new TimeControlParams();
            timeControlParams.waitingFor = waitingFor==null?-1L:waitingFor.timeInSec();
            timeControlParams.duration = maxDuration==null?-1L:maxDuration.timeInSec();

            timeControlParamsMap.put(step.name, timeControlParams);
        }








    }

    private class TimeControlParams{
        Long waitingFor = -1L;
        Long duration = -1L;


    }


}
