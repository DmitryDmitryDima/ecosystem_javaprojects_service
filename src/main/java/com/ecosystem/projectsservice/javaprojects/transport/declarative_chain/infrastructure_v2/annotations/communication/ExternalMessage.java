package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.communication;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


// аннотация для специальной версии цепочки, способной генерировать внешние события
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExternalMessage {
}
