package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


// данная аннотация предназначена для всех ивентов, записываемых в outbox - позволяет расшифровать payload
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface EventQualifier {
    String value();
}
