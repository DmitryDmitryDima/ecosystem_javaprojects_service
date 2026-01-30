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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class ControlledOutboxChain <E extends DeclarativeChainEvent<? extends ExternalEventContext,
        ? extends ExternalEventData,
        ? extends InternalEventData>> {

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
        if (externalResultType ==null) throw new IllegalStateException("Не указан тип внешнего ивента для цепи. Используйте @ExternalResultType");
        externalResultType = externalResultTypeAnno.event();
    }

    // кешируем и регистрируем chain state event
    private void cacheAndRegisterInternalEvent() throws Exception{
        Class<E> chainEventClass = (Class<E>) GenericTypeResolver.resolveTypeArgument(getClass(), OutboxDeclarativeChain.class);
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
            WaitingFor waitingFor = method.getAnnotation(WaitingFor.class);




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
                endingStep.waitingFor = waitingFor==null?null: waitingFor.timeInSec();
            }

            if (stepAnnotation!=null){
                CachedMethod cachedMethod = new CachedMethod();
                cachedMethod.method = method;
                cachedMethod.next = nextAnnotation.name();
                cachedMethod.maxRetry = maxRetry==null?0:maxRetry.maxCount();
                cachedMethod.message = message!=null;
                cachedMethod.name = stepAnnotation.name();

                cachedMethod.maxDuration = maxDuration==null?null:maxDuration.timeInSec();
                cachedMethod.waitingFor = waitingFor == null?null:waitingFor.timeInSec();

                steps.put(stepAnnotation.name(), cachedMethod);
            }


        }

        System.out.println(openingStep);
        System.out.println(steps);
        System.out.println(endingStep);


    }



    // с этого метода начинается каждый из процессов
    // метод входа в цепь, выбрасывает исключение при неправильности данных или ошибки записи в outbox
    // todo создание объекта процесса в оперативной памяти
    // изначальный currentStep = null
    public void init(E event) throws Exception{

        // создаем outbox объект
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setLast_update(Instant.now());
        outboxEvent.setType(internalEventQualifier);
        outboxEvent.setStatus(OutboxEvent.OutboxEventStatus.WAITING);

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



    // обработка ивента
    protected void processEvent(E event){
        // данный объект руководит состоянием ивента.
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



        // определяем, что сейчас будет исполнено
        CachedMethod toExecute = resolveNextExecution(internalEventData);

        // сценарий компенсации
        if (toExecute == null){
            performCompensationAndSendFinalErrorResult(event, chainProcess);
            return;
        }
        // todo сценарий остановки - пока не решил, объединять ли сценарии остановки и компеснации
        if (chainProcess.getStatus().get()== ChainProcess.ProcessStatus.STOPPED){
            onProcessStop(event, chainProcess);
        }



        chainProcess.stepOnStart(toExecute.name);
        internalEventData.setCurrentStep(toExecute.name);

        try {
            toExecute.method.invoke(this,event);

            if (toExecute==endingStep){
                successEndingStepScenario(event, chainProcess);
            }
            else {
                successStepScenario(event, toExecute, chainProcess);
            }
        }


        // в данном случае произошла остановка шага. Внутри шага мы должны все исключения, связанные с остановкой, делегировать в это исключение
        catch (StepInterruptedException e){
            onProcessStop(event, chainProcess);
        }

        // ошибка, не связанная с остановкой - логика, сбои т д. Требует ретрая, если он предусмотрен
        catch (Exception e){
            stepExecutionErrorScenario(event, toExecute, chainProcess, e.getCause().getMessage());
        }









    }
    // метод, используемый в цепочках для внутреннего контроля выполнения. В случае с запуском java приложения мы посылаем этот объект во всю обертку
    public ChainProcess getProcessState(UUID uuid){
        return processAggregator.getChainProcessByCorrelationId(uuid);
    }




    private void stepExecutionErrorScenario(E event, CachedMethod executed, ChainProcess chainProcess, String errorReason){
        chainProcess.processCleanup(ChainProcess.ProcessStatus.WAITING);

        InternalEventData internalEventData = event.getInternalData();
        internalEventData.setCurrentRetry(internalEventData.getCurrentRetry()+1);
        event.setMessage("ошибка выполнения шага "+executed.name+":Причина "+errorReason);

        try {
            OutboxEvent next = new OutboxEvent();
            next.setType(internalEventQualifier);
            next.setLast_update(Instant.now());
            next.setStatus(OutboxEvent.OutboxEventStatus.WAITING);
            next.setPayload(mapper.writeValueAsString(event));

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

    private void successStepScenario(E event, CachedMethod executed,  ChainProcess chainProcess){

        chainProcess.processCleanup(ChainProcess.ProcessStatus.WAITING);
        try {

            final OutboxEvent message = new OutboxEvent();
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

            }


            next.setLast_update(Instant.now());
            next.setType(internalEventQualifier);
            next.setStatus(OutboxEvent.OutboxEventStatus.WAITING);
            next.setPayload(mapper.writeValueAsString(event));

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
                externalEvent.setMessage("Процесс "+externalResultType.getName()+" успешно остановлен");

                OutboxEvent outboxEvent = new OutboxEvent();
                outboxEvent.setLast_update(Instant.now());
                outboxEvent.setType(externalEventQualifier);
                outboxEvent.setStatus(OutboxEvent.OutboxEventStatus.WAITING);
                outboxEvent.setPayload(mapper.writeValueAsString(externalEvent));

                transaction().execute(status -> {
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
        chainProcess.setStatus(ChainProcess.ProcessStatus.RUNNING);


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
                externalEvent.setStatus(EventStatus.ERROR);
                externalEvent.setMessage(event.getMessage());

                OutboxEvent outboxEvent = new OutboxEvent();
                outboxEvent.setLast_update(Instant.now());
                outboxEvent.setType(externalEventQualifier);
                outboxEvent.setStatus(OutboxEvent.OutboxEventStatus.WAITING);
                outboxEvent.setPayload(mapper.writeValueAsString(externalEvent));

            }
            catch (Exception e){
                // todo сценарий сбоя отправки error сообщения
            }




            transaction().execute(status -> {
                outboxCallback(event.getInternalData().getOutboxParent());
                return null;
            });
            chainProcess.terminate();
        }
    }


    private CachedMethod resolveNextExecution(InternalEventData internalEventData){



        String currentStep = internalEventData.getCurrentStep();
        long retry = internalEventData.getCurrentRetry();

        System.out.println("event catched. Current step is "+currentStep+" current retry is "+retry);

        // в зависимости от сценария ивент либо остается прежним, либо происходит поиск следующего
        CachedMethod eventStep;
        if (currentStep==null){
            eventStep = null;
        }
        else if (openingStep.name.equals(currentStep)){
            eventStep = openingStep;
        }
        else if (steps.containsKey(currentStep)){
            eventStep = steps.get(currentStep);
        }
        else {
            eventStep = endingStep;
        }


        CachedMethod toExecute;

        // тут выполняет стартовый шаг, первая итерация
        if (eventStep==null){
            toExecute = openingStep;
        }
        // количество ретраев превышает максимальное - это означает переход к компенсации
        else if (retry>eventStep.maxRetry){
            toExecute = null;
        }
        // механизм ретраев был запущен - выполняем метод в ивенте и обновляем счетчик
        else if (retry!=0) {
            toExecute = eventStep;
        }
        // переход к следюущему шагу. Если следующий шаг - конечный, то сразу отправляем сообщение и делаем новую транзакцию только при ошибке
        else {
            String next = eventStep.next;
            if (next.equals(endingStep.name)){
                toExecute = endingStep;
            }
            else {
                toExecute = steps.get(next);
            }
        }
        return toExecute;
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
        Long waitingFor = null;


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
