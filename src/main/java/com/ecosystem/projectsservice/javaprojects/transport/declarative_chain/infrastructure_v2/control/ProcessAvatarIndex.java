package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessAvatarIndex {


    // имя индекса
    private String name;

    // ключ поиска
    private String key;
}
