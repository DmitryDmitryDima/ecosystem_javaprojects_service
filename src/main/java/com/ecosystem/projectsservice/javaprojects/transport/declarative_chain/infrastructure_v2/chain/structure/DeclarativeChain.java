package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.control.Retry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.control.TimeLimit;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.control.WaitingForSignal;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.order.Ending;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.order.Opening;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.order.Step;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.ChainUtils;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.publisher.ChainPublisher;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.exception.ChainInitException;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.exception.ChainPreparationException;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorage;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessIndex;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEventProcessingInfo;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DeclarativeChain<E extends ChainEvent> {




    private static final long DEFAULT_READ_EXPIRATION_TIME_IN_SECONDS = 10;


    private static final long DEFAULT_PERFORMANCE_EXPIRATION_PERIOD_IN_SECONDS = 60*60;



    private ProcessRuntimeStorage processRuntimeStorage;



    private EventRegistry eventRegistry;

    private ChainPublisher chainPublisher;





    // прочитанное тело цепи, ассоциированное по имени
    private Map<String, ChainStep<?>> chainBody = new HashMap<>();

    // каждая цепь имеет точку входа и точку выхода
    private ChainStep<?> opening;

    private ChainStep<?> ending;



    public DeclarativeChain(ProcessRuntimeStorage processRuntimeStorage,
                            EventRegistry eventRegistry,
                            ChainPublisher chainPublisher) {

        this.processRuntimeStorage = processRuntimeStorage;
        this.eventRegistry = eventRegistry;
        this.chainPublisher = chainPublisher;
    }

    public DeclarativeChain(){

    }



    protected void setProcessRuntimeStorage(ProcessRuntimeStorage processRuntimeStorage) {
        this.processRuntimeStorage = processRuntimeStorage;
    }

    protected void setEventRegistry(EventRegistry eventRegistry) {
        this.eventRegistry = eventRegistry;
    }

    protected void setChainPublisher(ChainPublisher chainPublisher) {
        this.chainPublisher = chainPublisher;
    }

    // оставляем доступ к геттеру для модификации поведения при публикации
    protected ChainPublisher getChainPublisher() {
        return this.chainPublisher;
    }


    protected Map<String, ChainStep<?>> getChainBody() {
        return chainBody;
    }

    protected ChainStep<?> getOpening() {
        return opening;
    }

    protected ChainStep<?> getEnding() {
        return ending;
    }






    // метод переопределяется, если необходимо добавить дополнительное индексирование процесса
    protected List<ProcessIndex> setProcessIndexes(E event){
        return List.of();
    }

    // переопределение метода задает механизм,
    // согласно которому конкретный ивент попадает в конкретную цепочку
    public abstract void catchEvent(E event);

    // Действия при ошибке. На этом же этапе происходит, при нужной настройке, генерация внешнего сообщения
    protected abstract void compensationStrategy(E event);





    // хуки

    // onChainStop






    // кешируем и анализируем структуру цепочки, проверяем правильность ее конструкции
    public void prepareChain() throws ChainPreparationException {

        try {

            // регистрируем главный ивент
            registerChainEvent();



            // читаем структуру, на каждом из шагов вызываем хук onStepRead
            readChainStructure();







        }
        catch (Exception e){
            throw new ChainPreparationException("ошибка создания цепочки: "
                    +e.getMessage());
        }

    }

    // регистрация главного ивента (дефолт - через интерфейс EventRegistry
    protected void registerChainEvent(){



        Class clazz = (Class)((ParameterizedType)getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];


        eventRegistry.register(clazz);
    }

    // обработка структуры
    protected void readChainStructure(){





        Method[] allMethods = this.getClass().getDeclaredMethods();

        for (Method method:allMethods){



            Opening openingAnno = method.getAnnotation(Opening.class);
            Ending endingAnno = method.getAnnotation(Ending.class);
            Step step = method.getAnnotation(Step.class);

            Retry retry = method.getAnnotation(Retry.class);
            TimeLimit timeLimit = method.getAnnotation(TimeLimit.class);
            WaitingForSignal waitingForSignal = method.getAnnotation(WaitingForSignal.class);


            ChainStep<?> chainStep = new ChainStep<>();

            chainStep.setMethod(method);
            chainStep.setRetry(retry==null?0: retry.maxCount());

            chainStep.setTimeLimit(timeLimit==null?null: timeLimit.time());
            chainStep.setTimeLimitUnit(timeLimit == null? null : timeLimit.timeUnit());

            chainStep
                    .setWaitingForSignal(waitingForSignal == null?null: waitingForSignal.time());
            chainStep
                    .setWaitingForSignalUnit(waitingForSignal == null? null
                            : waitingForSignal.timeUnit());


            if (openingAnno!=null){
                chainStep.setName(openingAnno.name());
                chainStep.setNext(opening.getNext());

                opening = chainStep;

            }

            else if (endingAnno!=null){
                chainStep.setName(ending.getName());

                ending = chainStep;


            }

            else if (step!=null){
                chainStep.setName(step.name());
                chainStep.setNext(step.next());
                chainBody.put(chainStep.getName(), chainStep);
            }


            onStepRead(chainStep);

        }

        validateStructure();


    }

    // хук, срабатывающий при чтении шагов, позволяет добавить расширения для шагов
    protected void onStepRead(ChainStep<?> aReadStep){

    }


    // хук, если требуется валидировать структуру. Допишу его при введении циклов
    protected void validateStructure(){


        new BasicStructureValidator(getOpening(), getChainBody(), getEnding())
                .validateStructure();

    }







    // current шаг вычисляется перед публикацией outbox !
    // Соответственно в init проставляется current step == opening
    public void init(E event) throws ChainInitException {

        try {

            if (event == null){
                throw new IllegalStateException("missing event");
            }

            if (event.getProcessId() == null){
                throw new IllegalStateException("you should set process uuid");
            }

            // цепочка уже собрана и валидирована
            ChainEventProcessingInfo startingSettings = ChainEventProcessingInfo.builder()
                    .currentStep(opening.getName())
                    .build();
            event.setProcessingInfo(startingSettings);


            // duration первого шага

            Long duration = ChainUtils.convertToMillis(opening.getTimeLimit(),
                    opening.getTimeLimitUnit());


            // если performance expiration == null, то это означает Everlasting шаг
            ChainOutput output = ChainOutput.builder()
                    .event(event)
                    .status(OutboxEvent.OutboxEventStatus.WAITING)
                    .last_update(Instant.now())
                    .readExpiration(Instant.now()
                            .plusSeconds(DEFAULT_READ_EXPIRATION_TIME_IN_SECONDS))
                    .performanceExpirationPeriod(duration)
                    .build();


            // TODO ВЕЧНЫЙ ШАГ - ЗАДАЕТСЯ ЯВНО, ПРИ НЕМ PERFORMANCE EXPIRATION == NULL
            // ЕСЛИ АННОТАЦИИ EVERLASTING НЕТ, ТО ВЫСТАВЛЯЕТСЯ ЛИБО MAX_DURATION, либо default









        }

        catch (Exception e){
            throw new ChainInitException("Chain start fail "+e.getMessage());

        }





    }












    protected void processEvent(E event){

    }





















}
