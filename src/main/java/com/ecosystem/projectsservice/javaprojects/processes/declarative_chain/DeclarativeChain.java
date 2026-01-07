package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventContext;
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

public abstract class DeclarativeChain<E extends DeclarativeChainEvent<? extends ExternalEventContext>> {


    @Autowired
    private OutboxEventRepository outboxEventRepository;

    private String resultingEventType; // совпадает с именем state event'а

    private String internalEventQualifier;
    private String externalEventQualifier;



    @Autowired
    private ChainManager manager;


    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private CachedMethod openingStep;

    private CachedMethod endingStep;



    private final Map<String, CachedMethod> steps = new HashMap<>();

    // todo active process state cache associated by correlation id




    public TransactionTemplate transaction(){return transactionTemplate;}

    public abstract void configure() throws Exception;

    @PostConstruct
    public final void initiation() throws Exception{

       cacheResultingEventType();
       cacheAndRegisterInternalEvent();
       cacheExternalEvent();
       prepareSteps();

       // пользовательский конфтг
       configure();

    }

    // кешируем значение типа внешнего ивента
    private void cacheResultingEventType() throws Exception{
        // Название результирующего ивента необходимо как для расшифровки payload, так и для event_type во внешнем ивенте
        ExternalResultName externalResultName = this.getClass().getAnnotation(ExternalResultName.class);
        if (externalResultName ==null) throw new IllegalStateException("Не указан тип внешнего ивента для цепи. Используйте @ExternalResultName");
        resultingEventType = externalResultName.name();
    }

    // кешируем и регистрируем chain state event
    private void cacheAndRegisterInternalEvent() throws Exception{
        Class<E> chainEventClass = (Class<E>) GenericTypeResolver.resolveTypeArgument(getClass(), DeclarativeChain.class);
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
            System.out.println(method.getName());

            OpeningStep openingStepAnnotation = method.getAnnotation(OpeningStep.class);
            Step stepAnnotation = method.getAnnotation(Step.class);
            EndingStep endingAnnotation = method.getAnnotation(EndingStep.class);
            Next nextAnnotation = method.getAnnotation(Next.class);
            MaxRetry maxRetry = method.getAnnotation(MaxRetry.class);
            Message message = method.getAnnotation(Message.class);

            if (openingStepAnnotation!=null){
                openingStep = new CachedMethod();
                openingStep.maxRetry = maxRetry==null?0:maxRetry.maxCount();
                openingStep.message = message!=null;
                openingStep.next = nextAnnotation.name();
                openingStep.method = method;
                openingStep.name = openingStepAnnotation.name();
            }

            // в данном случае сообщение отправляется в любом случае
            if (endingAnnotation!=null){
                endingStep = new CachedMethod();
                endingStep.maxRetry = maxRetry==null?0:maxRetry.maxCount();
                endingStep.method = method;
                endingStep.name = endingAnnotation.name();
            }

            if (stepAnnotation!=null){
                CachedMethod cachedMethod = new CachedMethod();
                cachedMethod.method = method;
                cachedMethod.next = nextAnnotation.name();
                cachedMethod.maxRetry = maxRetry==null?0:maxRetry.maxCount();
                cachedMethod.message = message!=null;
                cachedMethod.name = stepAnnotation.name();

                steps.put(stepAnnotation.name(), cachedMethod);
            }


        }

        System.out.println(openingStep);
        System.out.println(steps);
        System.out.println(endingStep);


    }




    // метод входа в цепь, выбрасывает исключение при неправильности данных или ошибки записи в outbox
    // todo создание объекта процесса в оперативной памяти
    // изначальный currentStep = null
    public void init(E event) throws Exception{

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setLast_update(Instant.now());
        outboxEvent.setType(internalEventQualifier);
        outboxEvent.setStatus(OutboxEvent.OutboxEventStatus.WAITING);

        String payload = mapper.writeValueAsString(event);
        outboxEvent.setPayload(payload);

        transaction().execute(status -> {
            outboxEventRepository.save(outboxEvent);
            return null;
        });
    }














    // данный метод определяет, какой метод выполняется, базируясь на current step. При успешном выполнении current step проставляется на следующий
    protected final void processEvent(E event){

        // данный объект руководит состоянием ивента.
        InternalEventData internalEventData = event.getInternalData();




        try {

        }
        catch (Exception e){

        }

    }

    // механизм, позволяющей очереди ловить только свои ивенты
    public abstract void catchEvent(E event);



    private class CachedMethod{
        Method method;
        long maxRetry;
        String next;
        boolean message;
        String name;

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

    // сохраняем событие сообщения
    private void sendExternalResult(E internalEvent){
        ExternalEvent externalEvent = bindResultingEvent();
        externalEvent.setContext(internalEvent.getContext());
        externalEvent.setMessage(internalEvent.getMessage());
        externalEvent.setData(internalEvent.getExternalData());
        externalEvent.setType(resultingEventType);

    }

    // todo вызывай этот метод сначала и извлекай аннотацию для project event (это аннотация для конвертации, chain event type - > в payload)
    // связываем очередь с одним из результирующих ивентов
    protected abstract ExternalEvent bindResultingEvent();



}
