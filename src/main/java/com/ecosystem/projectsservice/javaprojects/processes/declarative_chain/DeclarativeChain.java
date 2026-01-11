package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain;

import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.*;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.EventStatus;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.external_events.markers.ProjectEvent;
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

        String currentStep = event.getInternalData().getCurrentStep();
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






        // вызов метода или компенсации
        boolean executionSuccess = true;
        internalEventData.setCurrentStep(toExecute==null?internalEventData.getCurrentStep():toExecute.name);
        try {
            if (toExecute==null){
                compensationStrategy(event);
            }
            else {

                toExecute.method.invoke(this,event);
            }



        }
        catch (Exception e){

            executionSuccess = false;
            // вызов компенсации выбрасывает ошибку - требуется отдельная обработка. Далее нужен только callback
            if (toExecute==null){
                // todo


            }
            else {
                // обновляем счетчик ретраев и сообщение. Если следующий шаг будет успешен, сообщение об ошибке сменится на нормальное
                internalEventData.setCurrentRetry(internalEventData.getCurrentRetry()+1);
                event.setMessage("ошибка выполнения шага "+toExecute.name+":Причина "+e.getCause().getMessage());
            }
        }

        // механизм взаимодействия с event state гарантирует, что там будет либо актуальное сообщение об успехе, либо об ошибке

        // коллбэк + новый outbox event
        try {

            if (executionSuccess){
                if (toExecute==endingStep){
                    ExternalEvent externalEvent = bindResultingEvent();
                    externalEvent.setContext(event.getContext());
                    externalEvent.setData(mapper.writeValueAsString(event.getExternalData()));
                    externalEvent.setType(resultingEventType);
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
                }
                // запись для следующего шага или финальное сообщение о фейле цепочки
                else {
                    // фейл цепочки
                    if (toExecute==null){
                        ExternalEvent externalEvent = bindResultingEvent();
                        externalEvent.setContext(event.getContext());
                        externalEvent.setData(mapper.writeValueAsString(event.getExternalData()));
                        externalEvent.setType(resultingEventType);
                        externalEvent.setStatus(EventStatus.ERROR);
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

                    }

                    else {
                        final OutboxEvent message = new OutboxEvent();
                        final OutboxEvent next = new OutboxEvent();
                        if (toExecute.message){

                            ExternalEvent externalEvent = bindResultingEvent();
                            externalEvent.setContext(event.getContext());
                            externalEvent.setData(mapper.writeValueAsString(event.getExternalData()));
                            externalEvent.setType(resultingEventType);
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
                            outboxCallback(internalEventData.getOutboxParent());
                            return null;
                        });



                    }
                }
            }

            // ошибка выполнения - счетчик retry выполнен, ошибка записана
            else {

                if (toExecute==null){
                    transaction().execute(status -> {
                        outboxCallback(event.getInternalData().getOutboxParent());
                        return null;
                    });

                    return;
                }

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


        }
        catch (Exception e){
            // todo ошибка записи в outbox таблицу или при маппинге payload
            e.printStackTrace();
        }



    }

    private void outboxCallback(long id){
        Optional<OutboxEvent> outboxEventCheck = outboxEventRepository.findById(id);
        outboxEventCheck.ifPresent(outbox->{
            outbox.setStatus(OutboxEvent.OutboxEventStatus.PROCESSED);
        });
    }

    // механизм, позволяющей очереди ловить только свои ивенты
    public abstract void catchEvent(E event);

    // механизм компенсации
    public abstract void compensationStrategy(E event);



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



    // todo вызывай этот метод сначала и извлекай аннотацию для project event (это аннотация для конвертации, chain event type - > в payload)
    // связываем очередь с одним из результирующих ивентов
    protected abstract ExternalEvent<? extends ExternalEventContext> bindResultingEvent();



}
