package com.ecosystem.projectsservice.javaprojects.dto.projects;


import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/*
построение проекта на основе готового системного шаблона
если инструкция приходит от пользователя или нейросети, то используется другой способ доставки template (из request)
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConstructorSettingsForSystemTemplateBuild {


    // путь к инструкции
    private String instructionsPath;

    private String fileTemplatesPath;



    private Project project;

    private ProjectType projectType;

    private boolean needEntryPoint;






}
