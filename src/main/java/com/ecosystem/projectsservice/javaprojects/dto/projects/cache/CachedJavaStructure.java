package com.ecosystem.projectsservice.javaprojects.dto.projects.cache;

// кешированная структура проекта, с указанием модификатора доступа и id каждого из классов
// возможно будет кешироваться только то, что находится в java директории

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CachedJavaStructure {





    private Map<String, List<CachedStructureJavaFile>> structure;

    private UUID id;






}
