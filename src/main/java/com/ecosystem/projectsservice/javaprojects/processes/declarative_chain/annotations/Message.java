package com.ecosystem.projectsservice.javaprojects.processes.declarative_chain.annotations;

// если есть, то отправляется сообщение с Processing статусом

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Message {
}
