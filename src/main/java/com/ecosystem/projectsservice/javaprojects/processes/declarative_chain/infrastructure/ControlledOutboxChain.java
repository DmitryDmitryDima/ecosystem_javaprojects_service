package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.infrastructure;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.exceptions.ChainInitiationException;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.exceptions.StepInterruptedException;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.EventStatus;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.ExternalEventData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.ChainProcess;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.ProcessAggregator;
import com.ecosystem.projectsservice.javaprojects.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class ControlledOutboxChain <E extends DeclarativeChainEvent<? extends ExternalEventContext,
        ? extends ExternalEventData,
        ? extends InternalEventData>> {


    // постоянные

    // данное значение предотвращает зависание процессов, в которых явно не указан @Duration
    private static final long DEFAULT_STEP_EXPIRATION_TIME_IN_SECONDS = 30;

    // зависимости, необходимые для работы фреймворка

    // канал для работы с outbox таблицей - сюда мы записываем следующие шаги, внешние сообщения и коллбэк для обработанных шагов
    @Autowired
    private OutboxEventRepository outboxEventRepository;

    // цепочка регистрирует тут свой state event
    @Autowired
    private ChainManager manager;

    // в цепочках намеренно используется программный путь описания транзакций - в условиях обилия операций, связанных с разными сервисами
    @Autowired
    private TransactionTemplate transactionTemplate;

    // конвертер для записи данных в бд
    @Autowired
    private ObjectMapper mapper;

    // агрегатор in memory process - state объектов
    @Autowired
    private ProcessAggregator processAggregator;

    // во всех цепочках используется данный геттер (желательно)
    public TransactionTemplate transaction(){return transactionTemplate;}


    // информация о внутренних и внешних ивентах

    // каждая цепочка - процесс предполагает некий результирующий (т.е. предназначенный для внешнего слушателя)
    // ивент в конце, тип которого указывается в аннотации @ExternalResultType
    private ExternalEventType externalResultType;

    // внутренние типы ивентов (внутреннего state ивента и результирующего внешнего ивента), записываемые в outbox
    private String internalEventQualifier;
    private String externalEventQualifier;


    // кешированные шаги

    private CachedMethod openingStep;

    private CachedMethod endingStep;

    private final Map<String, CachedMethod> steps = new HashMap<>();


    // связываем процесс с категорией ивента - результата (Project event from system, Project event from user, user personal event)
    protected abstract ExternalEvent<? extends ExternalEventContext> bindResultingEvent();

    // дополнительные параметры поиска для процесса, если требуется ассоциация не только с correlation id
    protected abstract void setProcessAssociations(E event);

    // механизм, позволяющей очереди ловить только свои ивенты
    public abstract void catchEvent(E event);

    // механизм компенсации
    public abstract void compensationStrategy(E event);


    //todo  если пользователь переопределяет этот метод, то формируется ручная конфигурация, в обход механизма аннотаций
    protected void configure() throws Exception{}


    // анализируем информацию о цепочке
    @PostConstruct
    public final void initiation() throws Exception{

        configure();

        // todo если configure переопределен - значит оставшаяся часть игнорируется
        cacheResultingEventType();
        cacheAndRegisterInternalEvent();
        cacheExternalEvent();
        prepareSteps();



    }


    // кешируем значение типа внешнего ивента
    private void cacheResultingEventType() throws Exception{
        // Название результирующего ивента необходимо как для расшифровки payload, так и для event_type во внешнем ивенте
        ExternalResultType externalResultTypeAnno = this.getClass().getAnnotation(ExternalResultType.class);
        if (externalResultTypeAnno==null) throw new IllegalStateException("Не указан тип внешнего ивента для цепи. Используйте @ExternalResultType");
        externalResultType = externalResultTypeAnno.event();
    }

    // кешируем и регистрируем chain state event
    private void cacheAndRegisterInternalEvent() throws Exception{
        Class<E> chainEventClass = (Class<E>) GenericTypeResolver.resolveTypeArgument(getClass(), ControlledOutboxChain.class);
        EventQualifier annotation = chainEventClass.getAnnotation(EventQualifier.class);
        if (annotation==null) throw new IllegalStateException("Не прописано имя внутреннего ивента для цепи. Используйте @EventQuailifier");
        internalEventQualifier = annotation.value();
        manager.registerInternalEvent(annotation.value(), chainEventClass);

    }

    // кешируем имя внешнего ивента
    private void cacheExternalEvent() throws Exception{
        Class<? extends ExternalEvent> externalEventClazz =  bindResultingEvent().getClass();
        EventQualifier annotation = externalEventClazz.getAnnotation(EventQualifier.class);
        if (annotation == null) throw new IllegalStateException("Не прописано имя внешнего ивента для цепи. Используйте @EventQualifier, сделайте bind");
        externalEventQualifier = annotation.value();
    }





    // анализируем структуру цепочки
    // todo добавить проверку корректности конфигурации
    private void prepareSteps(){
        Method[] allMethods = this.getClass().getDeclaredMethods();

        for (Method method:allMethods){


            OpeningStep openingStepAnnotation = method.getAnnotation(OpeningStep.class);
            Step stepAnnotation = method.getAnnotation(Step.class);
            EndingStep endingAnnotation = method.getAnnotation(EndingStep.class);
            Next nextAnnotation = method.getAnnotation(Next.class);
            MaxRetry maxRetry = method.getAnnotation(MaxRetry.class);
            Message message = method.getAnnotation(Message.class);

            MaxDuration maxDuration = method.getAnnotation(MaxDuration.class);
            WaitingPoint waitingPoint = method.getAnnotation(WaitingPoint.class);




            if (openingStepAnnotation!=null){
                openingStep = new CachedMethod();
                openingStep.maxRetry = maxRetry==null?0:maxRetry.maxCount();
                openingStep.message = message!=null;
                openingStep.next = nextAnnotation.name();
                openingStep.method = method;
                openingStep.name = openingStepAnnotation.name();
                openingStep.maxDuration = maxDuration==null?null:maxDuration.timeInSec();
            }

            // в данном случае сообщение отправляется в любом случае
            if (endingAnnotation!=null){
                endingStep = new CachedMethod();
                endingStep.maxRetry = maxRetry==null?0:maxRetry.maxCount();
                endingStep.method = method;
                endingStep.name = endingAnnotation.name();
                endingStep.maxDuration = maxDuration==null?null:maxDuration.timeInSec();
                endingStep.waitingPoint = waitingPoint ==null?null: waitingPoint.timeInSec();
            }

            if (stepAnnotation!=null){
                CachedMethod cachedMethod = new CachedMethod();
                cachedMethod.method = method;
                cachedMethod.next = nextAnnotation.name();
                cachedMethod.maxRetry = maxRetry==null?0:maxRetry.maxCount();
                cachedMethod.message = message!=null;
                cachedMethod.name = stepAnnotation.name();

                cachedMethod.maxDuration = maxDuration==null?null:maxDuration.timeInSec();
                cachedMethod.waitingPoint = waitingPoint == null?null: waitingPoint.timeInSec();

                steps.put(stepAnnotation.name(), cachedMethod);
            }


        }

        System.out.println(openingStep);
        System.out.println(steps);
        System.out.println(endingStep);


    }

    public void init(E event) throws Exception{
        // пробуем следующий подход - вычисляем следующий шаг перед публикацией outbox ивента, а не перед выполнением шага

        // проставляем начальный шаг
        event.getInternalData().setCurrentStep(openingStep.name);
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setLast_update(Instant.now());
        outboxEvent.setType(internalEventQualifier);
        outboxEvent.setStatus(OutboxEvent.OutboxEventStatus.WAITING);
        outboxEvent.setCorrelationId(event.getContext().getCorrelationId());

        // Duration параметры
        outboxEvent.setExpiredAt(Instant.now()
                .plusSeconds(Objects.requireNonNullElse(openingStep.maxDuration, DEFAULT_STEP_EXPIRATION_TIME_IN_SECONDS)));

        String payload = mapper.writeValueAsString(event);

        outboxEvent.setPayload(payload);

        // создаем process state объект
        // создаем процесс
        ChainProcess chainProcess = new ChainProcess(event.getContext().getCorrelationId(),
                externalResultType);


        transaction().execute(status -> {
            try {
                outboxEventRepository.save(outboxEvent);
                processAggregator.registerChainProcess(chainProcess);

                setProcessAssociations(event);
            }
            catch (Exception e){
                throw new ChainInitiationException("chain is not initiated. Reason "+e.getCause().getMessage());
            }
            return null;
        });
    }





    private CachedMethod findMethodByName(String name){

        if (openingStep.name.equals(name)) return openingStep;
        else if (endingStep.name.equals(name)) return endingStep;

        else {
            if (steps.containsKey(name)){
                return steps.get(name);
            }
            else {
                throw new IllegalStateException("method not found");
            }
        }
    }

    protected void processEvent(E event){
        // шаг выполнения уже вычислен
        // данный объект руководит состоянием ивента
        InternalEventData internalEventData = event.getInternalData();



        // объект управления процессом
        /*
        сценарий, когда chain process отсутствует, является защищенным - в данном случае мы должны воскресить state объект, базируясь на данных ивента
        ВАЖНО - если chain manager обнаруживает, что processing event завис и это было связано с падением jvm, то при восстановлении он должен обновить retry счетчик
         */
        ChainProcess chainProcess = processAggregator
                .getOrRestoreChainProcessByCorrelationId(event.getContext().getCorrelationId(),
                        new ChainProcess(event.getContext().getCorrelationId(), externalResultType),
                        ()-> setProcessAssociations(event)
                );

        // name всегда есть, но при достижении счетчика retry после выплнения шага мы должны явно указать, что достигнута компенсация
        // у компенсации свое время expireAt
        // - так мы разделим логику времени выполнения шага и компенсации

        if (internalEventData.isCompensationPhase()){
            performCompensationAndSendFinalErrorResult(event, chainProcess);
            return;
        }

        // если перед нами не компенсация, ищем его, проверяем стоп, выполняем метод

        String toExecuteName = internalEventData.getCurrentStep();


        CachedMethod toExecuteMethod = findMethodByName(toExecuteName);

        // todo сценарий остановки - пока не решил, объединять ли сценарии остановки и компеснации
        if (chainProcess.getStatus().get()== ChainProcess.ProcessStatus.STOPPED){
            event.setMessage("Процесс остановлен до выполнения шага "+toExecuteName);
            onProcessStop(event, chainProcess);
            return;
        }



        chainProcess.stepOnStart(toExecuteMethod.name);

        try {
            event.setMessage("Процесс запускает шаг "+toExecuteName);
            toExecuteMethod.method.invoke(this, event);

            if (toExecuteMethod==endingStep){
                successEndingStepScenario(event, chainProcess);
            }
            else {

                // в конце шага так же проверяем флаг
                if (chainProcess.getStatus().get()== ChainProcess.ProcessStatus.STOPPED){
                    System.out.println("Процесс остановлен после выполнения шага");
                    event.setMessage("Процесс остановлен после выполнения шага "+toExecuteName);
                    onProcessStop(event, chainProcess);
                    return;
                }

                successStepScenario(event, toExecuteMethod, chainProcess);
            }
        }

        catch (Exception e){
            if (e.getCause() instanceof StepInterruptedException){
                System.out.println("Процесс остановлен в момент выполнения шага");
                event.setMessage("Процесс остановлен в процессе выполнения шага "+toExecuteName);
                onProcessStop(event, chainProcess);
            }
            else {
                System.out.println("сбой логики - классическая ошибка - проброс на ретрай");
                event.setMessage("ошибка выполнения шага "+toExecuteName+". Причина "+e.getCause().getMessage());
                stepExecutionErrorScenario(event, toExecuteMethod, chainProcess);
            }
        }





    }




    // метод, используемый в цепочках для внутреннего контроля выполнения. В случае с запуском java приложения мы посылаем этот объект во всю обертку
    public ChainProcess getProcessState(UUID uuid){
        return processAggregator.getChainProcessByCorrelationId(uuid);
    }

    // задача этого сценария - зафиксировать, достигнуто ли состояние компенсации. Шаг сохраняется тот же
    private void stepExecutionErrorScenario(E event, CachedMethod executed, ChainProcess chainProcess){
        chainProcess.processCleanup(ChainProcess.ProcessStatus.WAITING);
        InternalEventData internalEventData = event.getInternalData();
        long currentRetry = internalEventData.getCurrentRetry()+1;
        internalEventData.setCurrentRetry(currentRetry);

        // достигнуто состояние компенсации
        if (currentRetry>executed.maxRetry){
            internalEventData.setCompensationPhase(true);
        }

        try {
            OutboxEvent next = new OutboxEvent();
            next.setType(internalEventQualifier);
            next.setLast_update(Instant.now());
            next.setStatus(OutboxEvent.OutboxEventStatus.WAITING);
            next.setPayload(mapper.writeValueAsString(event));
            next.setCorrelationId(event.getContext().getCorrelationId());

            // Duration параметры
            next.setExpiredAt(Instant.now()
                    .plusSeconds(Objects.requireNonNullElse(executed.maxDuration, DEFAULT_STEP_EXPIRATION_TIME_IN_SECONDS)));


            transaction().execute(status -> {
                outboxEventRepository.save(next);
                outboxCallback(internalEventData.getOutboxParent());
                return null;
            });

        }
        catch (Exception e){

        }

    }






    // сценарий успешного выполнения последнего шага
    private void successEndingStepScenario(E event, ChainProcess chainProcess){
        chainProcess.processCleanup(ChainProcess.ProcessStatus.STOPPED);



        try {
            ExternalEvent externalEvent = bindResultingEvent();
            externalEvent.setContext(event.getContext());
            externalEvent.setData(mapper.writeValueAsString(event.getExternalData()));
            externalEvent.setType(externalResultType.getName());
            externalEvent.setStatus(EventStatus.SUCCESS);
            externalEvent.setMessage(event.getMessage());

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setLast_update(Instant.now());
            outboxEvent.setType(externalEventQualifier);
            outboxEvent.setStatus(OutboxEvent.OutboxEventStatus.WAITING);
            outboxEvent.setPayload(mapper.writeValueAsString(externalEvent));
            outboxEvent.setCorrelationId(event.getContext().getCorrelationId());

            transaction().execute(status -> {
                outboxEventRepository.save(outboxEvent);
                outboxCallback(event.getInternalData().getOutboxParent());
                return null;
            });

            chainProcess.terminate();
        }
        catch (Exception e){
            // todo ошибка записи в аутбокс

        }
    }

    // задача этого метода - вычислить следующий шаг и записать его в outbox
    // в зависимости от наличия аннотаций времени необходимо задать соответствующие поля
    // duration на next - создается соответствующая запись expired_at в outbox либо в waiting object
    private void successStepScenario(E event, CachedMethod executed, ChainProcess chainProcess){
        chainProcess.processCleanup(ChainProcess.ProcessStatus.WAITING);

        CachedMethod nextMethod = findMethodByName(executed.next);

        // НЕ ЗАБЫВАЕМ СБРОСИТЬ КОМПЕНСАЦИОННЫЙ СЧЕТЧИК ДЛЯ СЛЕДУЮЩЕГО ШАГА
        event.getInternalData().setCurrentRetry(0);
        event.getInternalData().setCurrentStep(nextMethod.name);

        try {

            final OutboxEvent message = new OutboxEvent();

            // дальнейшая запись зависит от наличия waitingFor !
            final OutboxEvent next = new OutboxEvent();
            if (executed.message){

                ExternalEvent externalEvent = bindResultingEvent();
                externalEvent.setContext(event.getContext());
                externalEvent.setData(mapper.writeValueAsString(event.getExternalData()));
                externalEvent.setType(externalResultType.getName());
                externalEvent.setStatus(EventStatus.PROCESSING);
                externalEvent.setMessage(event.getMessage());



                message.setLast_update(Instant.now());
                message.setType(externalEventQualifier);
                message.setStatus(OutboxEvent.OutboxEventStatus.WAITING);
                message.setPayload(mapper.writeValueAsString(externalEvent));
                message.setCorrelationId(event.getContext().getCorrelationId());

            }


            next.setLast_update(Instant.now());
            next.setType(internalEventQualifier);
            next.setStatus(OutboxEvent.OutboxEventStatus.WAITING);
            next.setPayload(mapper.writeValueAsString(event));
            next.setCorrelationId(event.getContext().getCorrelationId());

            // Duration параметры
            next.setExpiredAt(Instant.now()
                    .plusSeconds(Objects.requireNonNullElse(nextMethod.maxDuration, DEFAULT_STEP_EXPIRATION_TIME_IN_SECONDS)));


            transaction().execute(status -> {
                if (message.getStatus()!=null){
                    outboxEventRepository.save(message);
                }
                outboxEventRepository.save(next);
                outboxCallback(event.getInternalData().getOutboxParent());
                return null;
            });
        }
        catch (Exception e){
            // todo ошибка записи в аутбокс
        }




    }




    private void onProcessStop(E event, ChainProcess chainProcess){
        chainProcess.processCleanup(ChainProcess.ProcessStatus.STOPPED);


        try {
            compensationStrategy(event);
        }
        catch (Exception e){
            // todo сценарий сбоя компенсации - требует отдельной обработки
        }
        finally {

            try{
                ExternalEvent externalEvent = bindResultingEvent();
                externalEvent.setContext(event.getContext());
                externalEvent.setData(mapper.writeValueAsString(event.getExternalData()));
                externalEvent.setType(externalResultType.getName());
                externalEvent.setStatus(EventStatus.SUCCESS);

                // внешняя система может оставить сообщение с причиной остановки
                String externalMessage = chainProcess.getExternalMessage().get();
                externalMessage = externalMessage==null?"":"Дополнительные сообщения: "+externalMessage;
                externalEvent.setMessage("Процесс "+externalResultType.getName()+" успешно остановлен. "+externalMessage);

                OutboxEvent outboxEvent = new OutboxEvent();
                outboxEvent.setLast_update(Instant.now());
                outboxEvent.setType(externalEventQualifier);
                outboxEvent.setStatus(OutboxEvent.OutboxEventStatus.WAITING);
                outboxEvent.setPayload(mapper.writeValueAsString(externalEvent));
                outboxEvent.setCorrelationId(event.getContext().getCorrelationId());

                transaction().execute(status -> {
                    outboxEventRepository.save(outboxEvent);
                    outboxCallback(event.getInternalData().getOutboxParent());
                    return null;
                });


                chainProcess.terminate();

            }
            catch (Exception e){
                // todo сценарий сбоя отправки error сообщения
            }





        }
    }






    private void performCompensationAndSendFinalErrorResult(E event, ChainProcess chainProcess){


        // null + running = компенсация
        //chainProcess.setStatus(ChainProcess.ProcessStatus.RUNNING); // противоречие - процесс может быть stopped


        try {
            compensationStrategy(event);
        }
        catch (Exception e){
            // todo сценарий сбоя компенсации - требует отдельной обработки
            e.printStackTrace();
        }
        finally {

            try{
                ExternalEvent externalEvent = bindResultingEvent();
                externalEvent.setContext(event.getContext());
                externalEvent.setData(mapper.writeValueAsString(event.getExternalData()));
                externalEvent.setType(externalResultType.getName());
                externalEvent.setStatus(EventStatus.ERROR);
                externalEvent.setMessage(event.getMessage());

                OutboxEvent outboxEvent = new OutboxEvent();
                outboxEvent.setLast_update(Instant.now());
                outboxEvent.setType(externalEventQualifier);
                outboxEvent.setStatus(OutboxEvent.OutboxEventStatus.WAITING);
                outboxEvent.setPayload(mapper.writeValueAsString(externalEvent));
                outboxEvent.setCorrelationId(event.getContext().getCorrelationId());

                transaction().execute(status -> {
                    outboxCallback(event.getInternalData().getOutboxParent());
                    outboxEventRepository.save(outboxEvent);
                    return null;
                });
                chainProcess.terminate();

            }
            catch (Exception e){
                // todo сценарий сбоя отправки error сообщения
                e.printStackTrace();
            }





        }
    }










    // уведомляем систему о том, что шаг был успешно обработан
    private void outboxCallback(long id){
        Optional<OutboxEvent> outboxEventCheck = outboxEventRepository.findById(id);
        outboxEventCheck.ifPresent(outbox->{
            outbox.setStatus(OutboxEvent.OutboxEventStatus.PROCESSED);
        });
    }


    // универсальный кеш дял методов всех цепочек
    private class CachedMethod{
        Method method;
        long maxRetry;
        String next;
        boolean message;
        String name;

        // optional параметры контроля времени
        Long maxDuration = null;
        Long waitingPoint = null;


        @Override
        public String toString() {
            return "CachedMethod{" +
                    "next='" + next + '\'' +
                    ", maxRetry=" + maxRetry +
                    ", message=" + message +
                    ", name='" + name + '\'' +
                    '}';
        }
    }







}
