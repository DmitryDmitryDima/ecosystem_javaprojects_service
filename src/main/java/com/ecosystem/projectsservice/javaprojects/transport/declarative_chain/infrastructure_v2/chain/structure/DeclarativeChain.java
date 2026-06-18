package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.event_registry.EventRegistry;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.exception.ChainPreparationException;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessIndex;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorage;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.EventManager;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.util.List;

public abstract class DeclarativeChain<E extends ChainEvent> {




    private static final long DEFAULT_READ_EXPIRATION_TIME_IN_SECONDS = 10;


    private static final long DEFAULT_PERFORMANCE_EXPIRATION_PERIOD_IN_SECONDS = 30;














    // рантайм регистратор процессов
    @Autowired
    private ProcessRuntimeStorage processRuntimeStorage;


    @Autowired
    private EventRegistry eventRegistry;

















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
    @PostConstruct
    public void onChainPreparing() throws ChainPreparationException {

        try {

        }
        catch (Exception e){
            throw new ChainPreparationException("ошибка создания цепочки: "
                    +e.getMessage());
        }

    }

    // регистрация главного ивента (дефолт - через интерфейс EventRegistry
    protected void registerChainEvent(E event){
        eventRegistry.register(event);
    }

    // обработка структуры
    protected void readChainStructure(){

    }

    // хук, срабатывающий при чтении шагов, позволяет добавить расширения для шагов
    protected void onStepRead(ChainStep<?> aReadStep, Method from){

    }





















}
