package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations;

import com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations.enums.StepTimeUnit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MaxDuration {

    long time();

    StepTimeUnit timeUnit() default StepTimeUnit.SEC;
}
