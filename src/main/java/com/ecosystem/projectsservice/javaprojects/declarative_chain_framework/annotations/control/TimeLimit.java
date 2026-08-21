package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.control;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.ChainTimeUnit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TimeLimit {

    long time();

    ChainTimeUnit timeUnit() default ChainTimeUnit.SEC;
}


