package com.ecosystem.projectsservice.javaprojects.processes.external_events;

public enum ExternalEventType {



    JAVA_PROJECT_FILE_SAVE("java_project_file_save"),
    JAVA_PROJECT_FILE_SAVE_SYSTEM("java_project_file_save_system"),
    JAVA_PROJECT_CREATION_FROM_TEMPLATE("java_project_creation_from_template"),
    JAVA_PROJECT_REMOVAL("java_project_removal"),

    JAVA_PROJECT_FILE_REMOVAL("java_project_file_removal"),
    JAVA_PROJECT_ADD_PARTICIPANT("java_project_participant_add"),
    JAVA_PROJECT_FILE_ADD("java_project_file_add"),
    JAVA_PROJECT_REMOVE_PARTICIPANT("java_project_participant_remove"),
    JAVA_PROJECT_ADD_DIRECTORY("java_project_directory_add"),
    JAVA_PROJECT_REMOVE_DIRECTORY("java_project_directory_remove");

    private final String name;

    ExternalEventType(String name){
        this.name = name;
    }

    public String getName(){return name;}

}
