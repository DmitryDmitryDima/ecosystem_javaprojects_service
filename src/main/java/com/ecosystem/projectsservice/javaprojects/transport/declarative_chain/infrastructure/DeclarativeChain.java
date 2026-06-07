package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure;

import com.ecosystem.projectsservice.javaprojects.transport.external_events.ExternalEvent;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.context.ExternalEventContext;
import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import jakarta.annotation.PostConstruct;

public abstract class DeclarativeChain<E extends DeclarativeChainEvent<? extends ExternalEventContext,
        ? extends ExternalEventData,
        ? extends InternalEventData>> {





    // метод переопределяется, если пользователь хочет автоматизировать общение цепочки с внешней ивент системой
    protected ExternalEvent<? extends ExternalEventContext> externalEvent(){
        return null;
    }

    // метод переопределяется, если необходимо добавить дополнительное индексирование процесса
    protected void setProcessAssociations(E event){}

    // переопределение метода задает механизм,
    // согласно которому конкретный ивент попадает в конкретную цепочку
    public abstract void catchEvent(E event);

    // действия при ошибке. На этом же этапе происходит, при нужной настройке, генерация внешнего сообщения
    protected abstract void compensationStrategy(E event);





    // хук, срабатывающий при ручной остановке процесса пользователем или внешним событием
    protected void onChainStop(E event){

    }




    // связываем процесс с категорией ивента - результата
    // (Project event from system, Project event from user, user personal event)
    protected ExternalEvent<? extends ExternalEventContext> bindResultingEvent(){
        return null;
    }

    // кешируем и анализируем структуру цепочки, проверяем правильность ее конструкции
    @PostConstruct
    private void initiation() throws Exception{

    }















}
