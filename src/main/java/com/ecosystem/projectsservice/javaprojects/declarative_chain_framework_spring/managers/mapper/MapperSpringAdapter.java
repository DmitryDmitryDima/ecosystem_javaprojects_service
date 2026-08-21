package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.managers.mapper;


import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.mapper.MapperComponent;
import tools.jackson.databind.ObjectMapper;


public class MapperSpringAdapter implements MapperComponent {


    // берется зависимость от spring

    private ObjectMapper springDefaultMapper;

    public MapperSpringAdapter(ObjectMapper springMapper){
        this.springDefaultMapper = springMapper;
    }


    @Override
    public String writeValueAsString(Object value) {
        return springDefaultMapper.writeValueAsString(value);
    }

    @Override
    public <V> V read(String str, Class<V> clazz) {
        return springDefaultMapper.readValue(str, clazz);
    }
}
