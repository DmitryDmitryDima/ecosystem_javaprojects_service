package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2;

import com.ecosystem.projectsservice.javaprojects.repository.OutboxEventRepository;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.DeclarativeChainEvent;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure.InternalEventData;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessIndex;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control.ProcessRuntimeStorage;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

public abstract class DeclarativeChain<E extends DeclarativeChainEvent<?
        extends ExternalEventContext,
        ? extends ExternalEventData,
        ? extends InternalEventData>> {

    // TODO РАЗОБРАТЬСЯ С НАЗНАЧЕНИЕМ ПОСТОЯННЫХ ВРЕМЕНИ И ПРОПИСАТЬ ИХ НАЗНАЧЕНИЕ ТУТ
    // константы
    private static final long DEFAULT_STEP_EXPIRATION_TIME_IN_SECONDS = 30;


    private static final long DEFAULT_READ_EXPIRATION_TIME_IN_SECONDS = 10;


    private static final long DEFAULT_PERFORMANCE_EXPIRATION_PERIOD_IN_SECONDS = 30;







    // ЗАВИСИМОСТИ

    // в standalone версии тут должен быть интерфейс
    @Autowired
    private OutboxEventRepository outboxEventRepository;


    // в цепочках намеренно используется программный
    // путь описания транзакций - в условиях обилия операций, связанных с разными сервисами
    @Autowired
    private TransactionTemplate transactionTemplate;

    // конвертер для записи данных в бд
    @Autowired
    private ObjectMapper mapper;

    // рантайм регистратор процессов
    @Autowired
    private ProcessRuntimeStorage processRuntimeStorage;






    // ОСНОВНЫЕ МЕТОДЫ

    // метод переопределяется,
    // если пользователь хочет автоматизировать общение цепочки с внешней ивент системой
    protected ExternalEvent<? extends ExternalEventContext> externalEvent(){
        return null;
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





    // хук, срабатывающий при ручной остановке процесса пользователем или внешним событием
    // может быть предназначен для сообщений
    protected void onChainStop(E event){

    }






    // кешируем и анализируем структуру цепочки, проверяем правильность ее конструкции
    @PostConstruct
    private void initiation() throws Exception{

    }















}
