package com.ecosystem.projectsservice.javaprojects.service.projects.state.code;



// инструменты анализа кода
public interface CodeService {


    // меняем package
    String transformPackage(String javaContent, String newPackageName);


    String transformFileConstructedPathToPackage(String constructedPath);


    String createEmptyPublicClass(String packagePath, String name);
}
