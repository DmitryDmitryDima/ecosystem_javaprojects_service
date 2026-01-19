package com.ecosystem.projectsservice.javaprojects.processes;

public enum ExternalEventType {



    JAVA_PROJECT_FILE_SAVE("java_project_file_save"),
    JAVA_PROJECT_FILE_SAVE_SYSTEM("java_project_file_save_system"),
    JAVA_PROJECT_CREATION_FROM_TEMPLATE("java_project_creation_from_template"),
    JAVA_PROJECT_REMOVAL("java_project_removal");

    private final String name;

    ExternalEventType(String name){
        this.name = name;
    }

    public String getName(){return name;}

}
