package com.ecosystem.projectsservice.javaprojects.service.projects.state;

public interface CodeService {


    // меняем package
    String transformPackage(String javaContent, String newPackageName) throws Exception;


    String createEmptyPublicClass(String packagePath, String name);
}
