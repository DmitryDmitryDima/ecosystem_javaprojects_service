package com.ecosystem.projectsservice.javaprojects.dto.projects.cache;


// кешированная структура java файлов (для предложек)
// сама выжимка из ast хранится в cached file

import com.ecosystem.projectsservice.javaprojects.service.projects.state.code.AccessModifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CachedStructureJavaFile {


    private UUID id;

    private AccessModifier modifier;




}
