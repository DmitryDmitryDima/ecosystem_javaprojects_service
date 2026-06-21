package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.control;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.enums.StepTimeUnit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DurationLimit {

    long time();

    StepTimeUnit timeUnit() default StepTimeUnit.SEC;
}
