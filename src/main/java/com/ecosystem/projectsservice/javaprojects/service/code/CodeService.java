package com.ecosystem.projectsservice.javaprojects.service.code;

public interface CodeService {


    // меняем package
    String transformPackage(String javaContent, String newPackageName) throws Exception;


    String createEmptyPublicClass(String packagePath, String name);
}
