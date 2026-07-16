package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.ChainTimeUnit;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.control.Everlasting;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.control.Retry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.control.TimeLimit;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.control.WaitingForSignal;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.order.Ending;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.order.Opening;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.order.Step;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.ChainUtils;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatarStatus;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.ChainOutput;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.OutputMetadata;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.OutputProcessor;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.output_actions.ChainInit;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.exception.ChainInitException;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.exception.ChainPreparationException;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatar;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatarStorage;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatarIndex;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEventProcessingInfo;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxStatus;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DeclarativeChain<E extends ChainEvent> {




    private static final long DEFAULT_READ_EXPIRATION_TIME_IN_SECONDS = 10;


    private static final long DEFAULT_PERFORMANCE_EXPIRATION_PERIOD_IN_SECONDS = 60*60;



    private ProcessAvatarStorage processAvatarStorage;



    private EventRegistry eventRegistry;

    private OutputProcessor outputProcessor;





    // прочитанное тело цепи, ассоциированное по имени
    private Map<String, ChainStep<?>> chainBody = new HashMap<>();

    // каждая цепь имеет точку входа и точку выхода
    private ChainStep<?> opening;

    private ChainStep<?> ending;



    public DeclarativeChain(ProcessAvatarStorage processAvatarStorage,
                            EventRegistry eventRegistry,
                            OutputProcessor outputProcessor) {

        this.processAvatarStorage = processAvatarStorage;
        this.eventRegistry = eventRegistry;
        this.outputProcessor = outputProcessor;
    }

    public DeclarativeChain(){

    }



    protected void setProcessRuntimeStorage(ProcessAvatarStorage processAvatarStorage) {
        this.processAvatarStorage = processAvatarStorage;
    }

    protected void setEventRegistry(EventRegistry eventRegistry) {
        this.eventRegistry = eventRegistry;
    }

    protected void setOutputProcessor(OutputProcessor outputProcessor) {
        this.outputProcessor = outputProcessor;
    }

    // оставляем доступ к геттеру для модификации поведения при публикации
    protected OutputProcessor getChainPublisher() {
        return this.outputProcessor;
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
    protected List<ProcessAvatarIndex> setProcessIndexes(E event){
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

            Everlasting everlasting = method.getAnnotation(Everlasting.class);


            ChainStep<?> chainStep = new ChainStep<>();

            chainStep.setMethod(method);
            chainStep.setRetry(retry==null?0: retry.maxCount());

            // сразу проставляем дефолт в случае отсутствия аннотации
            // значение лимита на выполнение игнорируется при наличии EVERLASTING
            chainStep.setTimeLimit(timeLimit==null?
                    DEFAULT_PERFORMANCE_EXPIRATION_PERIOD_IN_SECONDS: timeLimit.time());
            chainStep.setTimeLimitUnit(timeLimit == null? ChainTimeUnit.SEC : timeLimit.timeUnit());
            if (everlasting!=null){
                chainStep.setEverlasting(true);
            }



            chainStep
                    .setWaitingForSignal(waitingForSignal == null?null: waitingForSignal.time());
            chainStep
                    .setWaitingForSignalUnit(waitingForSignal == null? null
                            : waitingForSignal.timeUnit());




            if (openingAnno!=null){
                chainStep.setName(openingAnno.name());
                chainStep.setNext(openingAnno.next());

                opening = chainStep;

            }

            else if (endingAnno!=null){
                chainStep.setName(endingAnno.name());

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

        // aReadStep.setExtensions
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
            // если everlasting, то значение остается null
            // в противном случае указывается время на основании пользовательского значения, или дефолт
            Long duration = null;
            if (!opening.isEverlasting()){
                duration = ChainUtils.convertToMillis(opening.getTimeLimit(),
                        opening.getTimeLimitUnit());
            }




            // если performance expiration == null, то это означает Everlasting шаг
            ChainOutput output = ChainOutput.builder()
                    .event(event)
                    .status(OutboxStatus.WAITING)
                    .last_update(Instant.now())
                    .readExpiration(Instant.now()
                            .plusSeconds(DEFAULT_READ_EXPIRATION_TIME_IN_SECONDS))
                    .performanceExpirationPeriod(duration)
                    .build();



            OutputMetadata<?> metadata = new OutputMetadata<>();

            // не забываем проставить тип действия
            metadata.setAction(new ChainInit());

            onPublishChainOutput(output, metadata);



            // создаем runtime аватар процесса

            ProcessAvatar runtimeAvatar
                    = new ProcessAvatar(event.getProcessId());

            // добавляем индексы, вызывая переопределяемый метод
            runtimeAvatar.addIndexes(setProcessIndexes(event));

            runtimeAvatar.setStatus(ProcessAvatarStatus.WAITING);

            processAvatarStorage.registerChainProcess(runtimeAvatar);









        }

        catch (Exception e){
            throw new ChainInitException("Chain start fail "+e.getMessage());

        }





    }

    // выделяем хук для возможности добавления/модификации metadata
    protected void onPublishChainOutput(ChainOutput output,
                                        OutputMetadata<?> metadata){
        outputProcessor.output(output, metadata);


    }












    protected void processEvent(E event){

    }





















}
