package com.ecosystem.projectsservice.javaprojects.dto.dashboard;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IndexGroupDTO {

    private String name;

    // список вторичных ключей и uuid процессов

    // пример - id проекта, список процессов с uuid

    private Map<String, List<UUID>> buckets  = new HashMap<>();




}
