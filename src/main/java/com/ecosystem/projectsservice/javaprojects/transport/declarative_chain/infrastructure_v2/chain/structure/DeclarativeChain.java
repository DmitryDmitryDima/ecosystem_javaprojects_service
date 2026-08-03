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
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.OutputResult;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.output_actions.*;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.exception.*;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessAvatarStatus;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.status_groups.DeliveryStatus;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.status_groups.PerformanceStatus;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.ChainOutput;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.OutputMetadata;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output.OutputProcessor;
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






    protected void onCompensationStart(E event,
                                       ProcessAvatar avatar){

        // специальный статус для компенсации, в какой то степени помощник для аналога в бд
        // позволяет обнаружить, что runtime завис

        // если какой то шаг выполняется параллельно, то нужно убить то, что он пометил в аватаре
        avatar.performActionsAndSetStatus(ProcessAvatarStatus.COMPENSATING, null ,null);

    }


    protected void onCompensationEnd(E event, CompensationResult result,
                                     ProcessAvatar avatar){


        OutputMetadata<?> meta = new OutputMetadata<>();

        meta.setAction(new CompensationEnd());



        ChainOutput output = new ChainOutput();

        output.setEvent(event);


        // финальный коллбэк плюс убийство аватара
        onPublishChainOutput(output, meta, avatar);



    }

    protected void compensationDecorator(E event, ProcessAvatar avatar){
        onCompensationStart(event, avatar);


        CompensationResult compensationResult = new CompensationResult();

        try {
            compensationStrategy(event);
        }
        catch (Exception e){

            compensationResult.setException(e);

        }

        onCompensationEnd(event, compensationResult, avatar);






    }






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
            // в противном случае указывается время на основании
            // пользовательского значения, или дефолт
            Long duration = null;
            if (!opening.isEverlasting()){
                duration = ChainUtils.convertToMillis(opening.getTimeLimit(),
                        opening.getTimeLimitUnit());
            }


            // создаем runtime аватар процесса

            ProcessAvatar runtimeAvatar
                    = new ProcessAvatar(event.getProcessId());

            // добавляем индексы, вызывая переопределяемый метод
            runtimeAvatar.addIndexes(setProcessIndexes(event));

            runtimeAvatar.setStatus(ProcessAvatarStatus.WAITING);

            processAvatarStorage.registerAvatar(runtimeAvatar);




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




            OutputResult result = onPublishChainOutput(output, metadata, runtimeAvatar);

            if (!result.isPublished()){
                throw new IllegalStateException(result.getMessage());
            }













        }

        catch (Exception e){
            throw new ChainInitException("Не удалось запустить цепь "+e.getMessage());
        }





    }

    // выделяем хук для возможности добавления/модификации metadata
    protected OutputResult onPublishChainOutput(ChainOutput output,
                                                OutputMetadata<?> metadata, ProcessAvatar avatar){
        return outputProcessor.output(output, metadata, avatar);


    }











    /* ПРИНЦИПЫ
     - ПОПАДАНИЕ В ЭТУ СТАДИЮ ОЗНАЧАЕТ, ЧТО АВАТАР МОЖЕТ БЫТЬ ВОСКРЕШЕН,
        ДАЖЕ ДЛЯ КОМПЕНСАЦИИ

     - Шаг для выполнения вычисляется в конце предыдущего действия

     - Каждый этап должен иметь переопределяемый protected хук



     // todo реализовать возможность создавать шаги с аватаром в качестве параметра
          (альтернатива геттеру)

    */


    protected void processEvent(E event){


        // ГОТОВИМ БАЗОВУЮ ИНФОРМАЦИЮ ОБ ИВЕНТЕ

        if (event.getProcessId() == null)
            throw new ChainStepExecutionException("Невозможно выполнить шаг, отсутствует uuid процесса");


        var info = event.getProcessingInfo();

        if (info == null) throw new ChainStepExecutionException("Невозможно обработать шаг," +
                " отсутствует необходимый state");










        // восстанавливаем или получаем текущий runtime аватар
        ProcessAvatar avatar = processAvatarStorage.getOrRestore(event.getProcessId(),
                new ProcessAvatar(event.getProcessId(), setProcessIndexes(event)));













        // АНАЛИЗИРУЕМ DELIVERY И PERFORMANCE STATUS для определения следующего действия
        DeliveryStatus deliveryStatus = info.getDeliveryStatus();

        PerformanceStatus performanceStatus = info.getPerformanceStatus();


        // все статусу, кроме success, предполагают по дефолту компенсационный сценарий
        // для гибкости для каждого из них выделяем хук-сценарий, вызывающий компенсацию


        /*
            Предыдущий шаг не смог корректно опубликоваться.
             Нужна компенсация (решение о retry принимает пользователь)
         */
        if (deliveryStatus == DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_CRASH
                || deliveryStatus == DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_STEP
                || deliveryStatus == DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_STOP
                || deliveryStatus == DeliveryStatus.OUTBOX_PROCESSOR_ERROR_AFTER_FINAL_STEP



        ){

            outputErrorCompensationScenario(event, avatar);

        }

        else if (deliveryStatus == DeliveryStatus.EXPIRED_WAITING_FOR_SIGNAL){

            expiredWaitingForSignalCompensationScenario(event, avatar);

        }

        else if (deliveryStatus == DeliveryStatus.EVERLASTING_STEP_MISSING_CONTEXT){
            everlastingStepMissingContextCompensationScenario(event, avatar);
        }

        else if (deliveryStatus == DeliveryStatus.EXPIRED_PROCESSING_MISSING_CONTEXT){
            expiredProcessingMissingContextCompensationScenario(event, avatar);
        }

        else if (deliveryStatus == DeliveryStatus.EXPIRED_READING){
            expiredReadingCompensationScenario(event, avatar);

        }

        else if (deliveryStatus == DeliveryStatus.EXPIRED_PROCESSING_WITH_CONTEXT){

            expiredProcessingWithContextCompensationScenario(event, avatar);
        }


        // успешное чтение
        else if (deliveryStatus == DeliveryStatus.SUCCESS_READING){



            // stopped && crashed - компенсационные сценарии

            if (performanceStatus == PerformanceStatus.CRASHED){
                compensationAfterCrashScenario(event, avatar);
            }

            else if (performanceStatus == PerformanceStatus.STOPPED_BEFORE_STEP
                    || performanceStatus == PerformanceStatus.STOPPED_AFTER_STEP
                    || performanceStatus == PerformanceStatus.STOPPED_DURING_STEP
            ){
                compensationAfterStopScenario(event, avatar);
            }

            else {
                stepExecutionScenario(event, avatar);
            }




        }








    }


    protected void stepExecutionScenario(E event, ProcessAvatar avatar){







        var info = event.getProcessingInfo();


        ChainStep<?> step = findStepByName(info.getCurrentStep());

        if (step == null) throw new ChainStepExecutionException("не найден шаг для выполнения");


        // обрабатываем ситуацию, когда система обнаруживает,
        // что процесс был остановлен до выполнения следующего шага

        if (avatar.getStatus().get() == ProcessAvatarStatus.STOPPED){

            stepStopBeforeExecutionScenario(event, avatar, step);

            return;

        }


        // выполняем шаг

        // аватар фиксирует поток выполнения, а также статус running, вместе с названием выполняемого шага
        avatar.stepOnStart(step.getName());

        try {
            // todo добавить возможность инъекции аватара в метод,
            //  чтобы пользователю было удобно получать доступ к нему
            step.getMethod().invoke(this, event);





        }
        catch (Exception exception){


            // пользователь должен явно выбрасывать исключение такого типа, чтобы сообщить системе,
            // что процесс был прерван в момент выполнения
            if (exception.getCause() instanceof StepStoppedDuringExecutionException){

                stepStopDuringExecutionScenario(event, avatar, step);

                return;

            }

            else {

                // ошибка бизнес логики.

                stepExecutionErrorScenario(event, avatar,step, exception);



                return;
            }







        }

        // проверка на стоп после выполнения


        if (avatar.getStatus().get() == ProcessAvatarStatus.STOPPED){
            stepStopAfterStepExecutionScenario(event, avatar, step);
            return;
        }

        // success scenario

        // если шаг был конечным в цепи
        if (step == ending){
            stepExecutionSuccessEndingScenario(event, avatar, step);
        }

        // промежуточный шаг
        else {
            stepExecutionSuccessStepScenario(event, avatar, step);
        }












    }

    // последний шаг выполнен успешно
    // todo хук завершения всего процесса
    protected void stepExecutionSuccessEndingScenario(E event,
                                                      ProcessAvatar avatar,
                                                      ChainStep<?> step){



        OutputMetadata<?> metadata = new OutputMetadata<>();

        metadata.setAction(new ChainEnd());
        metadata.setExecutedStep(step);


        ChainOutput chainOutput = ChainOutput.builder()
                .event(event)
                .build();


        onPublishChainOutput(chainOutput, metadata, avatar);





    }

    // промежуточный шаг выполнен успешно


    protected void stepExecutionSuccessStepScenario(E event,
                                                    ProcessAvatar avatar,
                                                    ChainStep<?> step){

        // не забываем сбросить счетчик ретраев для следующего шага

        // вычисляем next шаг.
        // todo Учитываем, что при ошибке публикации в ивенте в current будет именно следующий шаг


        ChainStep<?> next = findStepByName(step.getNext());

        ChainEventProcessingInfo info = event.getProcessingInfo();

        if (next == null) throw new ChainScenarioException("следующий шаг не найден после "+step.getName());


        info.setPerformanceStatus(PerformanceStatus.STEP_PERFORMED);
        info.setCurrentStep(next.getName());
        info.setCurrentRetry(0); // сбрасываем счетчик на случай, если мы вошли сюда после retry фазы


        OutputMetadata<?> meta = new OutputMetadata<>();
        meta.setExecutedStep(step);
        meta.setAction(new ChainOpeningOrMiddleStep());



        Long duration = null;
        if (!next.isEverlasting()){
            duration = ChainUtils.convertToMillis(step.getTimeLimit(),
                    step.getTimeLimitUnit());
        }


        // todo учесть, что шаг может быть waiting for signal

        // для waiting for signal (external) необходима конвертация
        Instant readExpiration = next.getWaitingForSignal()
                == null?Instant.now().plusSeconds(DEFAULT_READ_EXPIRATION_TIME_IN_SECONDS)
                :Instant.now().plusMillis(ChainUtils.convertToMillis(next.getWaitingForSignal(),
                next.getWaitingForSignalUnit()));



        ChainOutput output = ChainOutput.builder()
                .event(event)
                .status(next.getWaitingForSignal() == null?OutboxStatus.WAITING
                        :OutboxStatus.WAITING_FOR_EXTERNAL)
                .last_update(Instant.now())
                .readExpiration(readExpiration)
                .performanceExpirationPeriod(duration)
                .build();



        onPublishChainOutput(output, meta, avatar);



    }

    // выполнение шага завершилось ошибкой
    protected void stepExecutionErrorScenario(E event,
                                              ProcessAvatar avatar,
                                              ChainStep<?> step,
                                              Exception e){


        var info = event.getProcessingInfo();

        // todo причина ошибки
        event.setMessage("Выполнение шага "+info.getCurrentStep()+

                "завершилось ошибкой "
                );




        // количество ретраев исчерпано - процесс получает статус crashed, генерируется компенсационный ивент
        if (info.getCurrentRetry()>=step.getRetry()){

            info.setPerformanceStatus(PerformanceStatus.CRASHED);

            ChainOutput output = ChainOutput.builder()
                    .event(event)
                    .status(OutboxStatus.WAITING)
                    .last_update(Instant.now())
                    .readExpiration(Instant.now()
                            .plusSeconds(DEFAULT_READ_EXPIRATION_TIME_IN_SECONDS))
                    .performanceExpirationPeriod(DEFAULT_PERFORMANCE_EXPIRATION_PERIOD_IN_SECONDS)
                    .build();



            OutputMetadata<?> metadata = new OutputMetadata<>();

            // не забываем проставить тип действия для процессора
            metadata.setAction(new ChainCrash());


            onPublishChainOutput(output,
                    metadata,
                    avatar);



        }

        // можно отправить на ретрай
        else {

            // обновляем счетчик
            info.setCurrentRetry(info.getCurrentRetry()+1);

            // фиксируем перформанс статус
            info.setPerformanceStatus(PerformanceStatus.STEP_RETRY);


            Long duration = null;
            if (!step.isEverlasting()){
                duration = ChainUtils.convertToMillis(step.getTimeLimit(),
                        step.getTimeLimitUnit());
            }

            // для waiting for signal (external) необходима конвертация
            Instant readExpiration = step.getWaitingForSignal()
                    == null?Instant.now().plusSeconds(DEFAULT_READ_EXPIRATION_TIME_IN_SECONDS)
                    :Instant.now().plusMillis(ChainUtils.convertToMillis(step.getWaitingForSignal(),
                    step.getWaitingForSignalUnit()));


            // если performance expiration == null, то это означает Everlasting шаг
            ChainOutput output = ChainOutput.builder()
                    .event(event)
                    .status(step.getWaitingForSignal()==null?OutboxStatus.WAITING
                            :OutboxStatus.WAITING_FOR_EXTERNAL)
                    .last_update(Instant.now())
                    .readExpiration(readExpiration)
                    .performanceExpirationPeriod(duration)
                    .build();



            OutputMetadata<?> metadata = new OutputMetadata<>();

            // не забываем проставить тип действия для процессора
            metadata.setAction(new ChainOpeningOrMiddleStep());


            onPublishChainOutput(output,
                    metadata,
                    avatar);








        }

    }


    // данный сценарий может сработать также в случае,
    // если пользователь не использовал мониторинг аватара при выполнении шага
    protected void stepStopAfterStepExecutionScenario(E event,
                                                      ProcessAvatar avatar,
                                                      ChainStep<?> step
                                                               ){

        event.setMessage("Остановка процесса после выполнения шага "+event.getProcessingInfo().getCurrentStep());

        event.getProcessingInfo().setPerformanceStatus(PerformanceStatus.STOPPED_AFTER_STEP);

        // настройки времени берутся не от шага, а от компенсации (сейчас - дефолтные)

        // компенсация не может быть everlasting
        ChainOutput output = ChainOutput.builder()
                .event(event)
                .status(OutboxStatus.WAITING)
                .last_update(Instant.now())
                .readExpiration(Instant.now()
                        .plusSeconds(DEFAULT_READ_EXPIRATION_TIME_IN_SECONDS))
                .performanceExpirationPeriod(DEFAULT_PERFORMANCE_EXPIRATION_PERIOD_IN_SECONDS)
                .build();



        OutputMetadata<?> metadata = new OutputMetadata<>();

        // не забываем проставить тип действия
        metadata.setAction(new ChainStop());




        onPublishChainOutput(output, metadata, avatar);




    }


    protected void stepStopDuringExecutionScenario(E event,
                                                   ProcessAvatar avatar,
                                                   ChainStep<?> step){

        event.setMessage("Остановка процесса во время шага "+event.getProcessingInfo().getCurrentStep());

        event.getProcessingInfo().setPerformanceStatus(PerformanceStatus.STOPPED_DURING_STEP);

        // настройки времени берутся не от шага, а от компенсации (сейчас - дефолтные)

        // компенсация не может быть everlasting
        ChainOutput output = ChainOutput.builder()
                .event(event)
                .status(OutboxStatus.WAITING)
                .last_update(Instant.now())
                .readExpiration(Instant.now()
                        .plusSeconds(DEFAULT_READ_EXPIRATION_TIME_IN_SECONDS))
                .performanceExpirationPeriod(DEFAULT_PERFORMANCE_EXPIRATION_PERIOD_IN_SECONDS)
                .build();



        OutputMetadata<?> metadata = new OutputMetadata<>();

        // не забываем проставить тип действия
        metadata.setAction(new ChainStop());




        onPublishChainOutput(output, metadata, avatar);



    }



    // сценарий, когда процесс был остановлен до выполнения следующего шага
    // создается новый компенсационный ивент со специальным performance status

    protected void stepStopBeforeExecutionScenario(E event,
                                                   ProcessAvatar avatar,
                                                   ChainStep<?> step){


        event.setMessage("Остановка процесса до шага "+event
                .getProcessingInfo()
                .getCurrentStep());

        // меняем performance статус для следующей итерации цепи - будет запущена компенсация
        event.getProcessingInfo().setPerformanceStatus(PerformanceStatus.STOPPED_BEFORE_STEP);



        // настройки времени берутся не от шага, а от компенсации (сейчас - дефолтные)

        // компенсация не может быть everlasting
        ChainOutput output = ChainOutput.builder()
                .event(event)
                .status(OutboxStatus.WAITING)
                .last_update(Instant.now())
                .readExpiration(Instant.now()
                        .plusSeconds(DEFAULT_READ_EXPIRATION_TIME_IN_SECONDS))
                .performanceExpirationPeriod(DEFAULT_PERFORMANCE_EXPIRATION_PERIOD_IN_SECONDS)
                .build();



        OutputMetadata<?> metadata = new OutputMetadata<>();

        // не забываем проставить тип действия
        metadata.setAction(new ChainStop());




        onPublishChainOutput(output, metadata, avatar);


    }






    // хук, срабатывающий при компенсационном сценарии
    protected void outputErrorCompensationScenario(E event,
                                                   ProcessAvatar avatar){




        compensationDecorator(event, avatar);

    }
    // хук, срабатывающий при компенсационном сценарии
    protected void expiredWaitingForSignalCompensationScenario(E event, ProcessAvatar avatar){

        compensationDecorator(event, avatar);

    }
    // хук, срабатывающий при компенсационном сценарии
    protected void everlastingStepMissingContextCompensationScenario(E event, ProcessAvatar avatar){

        compensationDecorator(event, avatar);
    }

    // хук, срабатывающий при компенсационном сценарии
    protected void expiredProcessingMissingContextCompensationScenario(E event, ProcessAvatar avatar){

        compensationDecorator(event, avatar);
    }
    // хук, срабатывающий при компенсационном сценарии
    protected void expiredReadingCompensationScenario(E event, ProcessAvatar avatar){

        compensationDecorator(event, avatar);
    }

    // хук, срабатывающий при компенсационном сценарии
    protected void expiredProcessingWithContextCompensationScenario(E event, ProcessAvatar avatar){

        compensationDecorator(event, avatar);
    }


    // сценарий компенсации для остановленного процесса.
    // После обнаружения сигнала stop цепь создает запись с performance status stopped,
    // тем самым отделяя компенсацию от performance
    protected void compensationAfterStopScenario(E event, ProcessAvatar avatar){
        compensationDecorator(event, avatar);
    }


    // сценарий компенсации для сломавшегося процесса
    // после исчерпания retry счетчика процесс получает crashed в performance status,
    // после чего создает компенсационный ивент, тем самым отделяя компенсацию от хода выполнения
    protected void compensationAfterCrashScenario(E event, ProcessAvatar avatar){
        compensationDecorator(event, avatar);
    }










    // метод поиска мета информации о шаге согласно имени


    protected ChainStep<?> findStepByName(String name){

         if (opening.getName().equals(name)) return opening;

         else if (ending.getName().equals(name)) return ending;

         else {

             return chainBody.get(name);

         }


    }





















}
