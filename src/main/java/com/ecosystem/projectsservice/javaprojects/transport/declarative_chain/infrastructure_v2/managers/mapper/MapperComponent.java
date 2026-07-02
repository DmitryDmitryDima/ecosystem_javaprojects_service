package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.mapper;


// маппер ответственен за запись ивента цепи в строковом формате, а также за чтение
// записанной строки по типу ивента
public interface MapperComponent {


    String writeValueAsString(Object value);


    <V> V read(String str, Class<V> clazz);

}
