package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.registry;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface ChainEventQualifier {

    String value();
}
