package com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.constructors;


import com.ecosystem.projectsservice.javaprojects.model.File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileTemplateEnvelope {

    private File file;

    // сразу загружаем контент
    private String templateContent = "";


}
