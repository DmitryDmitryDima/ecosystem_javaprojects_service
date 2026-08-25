package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.control;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.ChainTimeUnit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



// позволяет ввести отсрочку для чтения (использование lock_until)


/*

Данный лок, по сути, активируется с момента перехода в waiting статус.

При наличии лока время устаревания ивента при чтении высчитывается как instant.now() + lock_period + readExpiration

поле lock_until выставляется в instant.now() + lock_period

Концептуальная сложность при комбинации с waiting_for_signal статусом
- данный статус устанавливает read_expiration,
который является в том числе точкой во времени, до которой система ждет внешней реакции.
Как только приходит внешняя реакция, выставляется статус waiting,
который опирается на то же самое значение read_expiration (оно никак не меняется при смене статуса).

Получается, что в существующей системе lock не может быть реализован по схеме выше,
 так как выходит, что он должен "продлить" уже заданный read_expiration.
  Таким образом, при наличии waiting_for_signal read_lock либо невозможен, либо должен быть другой механизм работы
 */


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReadLock {


    long time();

    ChainTimeUnit timeUnit() default ChainTimeUnit.SEC;
}
