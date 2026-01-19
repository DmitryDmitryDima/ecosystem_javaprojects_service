package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations;

import com.ecosystem.projectsservice.javaprojects.processes.ExternalEventType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExternalResultType {
    ExternalEventType event();
}
