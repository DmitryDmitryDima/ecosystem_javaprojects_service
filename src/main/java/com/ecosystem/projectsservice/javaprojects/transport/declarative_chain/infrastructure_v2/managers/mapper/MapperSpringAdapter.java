package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.mapper;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class MapperSpringAdapter implements MapperComponent{

    @Autowired
    private ObjectMapper springDefaultMapper;


    @Override
    public String writeValueAsString(Object value) {
        return springDefaultMapper.writeValueAsString(value);
    }

    @Override
    public <V> V read(String str, Class<V> clazz) {
        return springDefaultMapper.readValue(str, clazz);
    }
}
