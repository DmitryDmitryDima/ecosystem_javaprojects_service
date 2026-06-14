package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


// класс + метаданные
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventEnvelope {

    private Class<?> clazz;

    private String eventQualifier;


    private EventCategory eventCategory;



    public static enum EventCategory{
        INTERNAL, EXTERNAL
    }



}
