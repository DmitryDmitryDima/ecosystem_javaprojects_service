package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.order;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
каждый шаг имеет рукописное имя
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Opening {


    String name();

    String next();


}
