package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.annotations.control;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


// опасная аннотация, так как позволяет создавать бесконечные шаги
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Everlasting {
}
