package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.MaxDuration;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.WaitingFor;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.ChainProcess;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.ProcessAggregator;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
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

    // регистрация нативного cmd процесса - обязательно для ситуаций, когда таковые имеются в шаге
    protected void registerNativeProcess(UUID correlationId, Process process){

    }

    // получение объекта ChainProcess для контроля метода
    protected ChainProcess getChainProcessState(UUID correlationId){
        return processAggregator.getChainProcessByCorrelationId(correlationId);
    }



    @Override
    protected void specificPreparation() {
        super.specificPreparation();
        extractTimeControlParams();

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
