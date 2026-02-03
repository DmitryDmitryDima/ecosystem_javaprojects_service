package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

public enum TriggerType {

    SIMPLE_USER_CONTROLLED_PROJECT_TRIGGER("simple_user_controlled_project_trigger");

    private String value;


    TriggerType(String type){
        this.value = type;
    }

    public String getValue(){
        return value;
    }
}
