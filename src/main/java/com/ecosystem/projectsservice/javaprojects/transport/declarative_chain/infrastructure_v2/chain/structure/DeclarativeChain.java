package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.order.Ending;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.order.Opening;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.publisher.ChainPublisher;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.exception.ChainPreparationException;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorage;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessIndex;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorageImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class DeclarativeChain<E extends ChainEvent> {




    private static final long DEFAULT_READ_EXPIRATION_TIME_IN_SECONDS = 10;


    private static final long DEFAULT_PERFORMANCE_EXPIRATION_PERIOD_IN_SECONDS = 30;



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













            // onStepRead(step)





        }

        validate();







    }

    // хук, срабатывающий при чтении шагов, позволяет добавить расширения для шагов
    protected void onStepRead(ChainStep<?> aReadStep, Method from){

    }

    // хук валидации - работает с геттерами

    protected void validate(){

    }





















}
