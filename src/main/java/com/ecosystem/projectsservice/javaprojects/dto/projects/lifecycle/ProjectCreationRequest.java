package com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle;

import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectPrivacyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectCreationRequest {

    @NotNull
    @NotBlank
    private String name;




    // галочка "сгенерировать главный класс"
    private boolean needEntryPoint;

    private ProjectPrivacyLevel privacyLevel;

    private String projectType; // в будущем можно будет выбрать шаблон проекта

    // Experimental. Если отсутствует, то создаем проект по готовой инструкции
    private String prompt;


}
