package com.ecosystem.projectsservice.javaprojects.exceptions;

/*
исключение, выбрасываемое в ситуации, когда кто то птыается получить доступ к ресурсу, открытому только для разрешенного пользователем кругу лиц
 */
public class AccessDeniedException extends Exception {


    public AccessDeniedException(String message){
        super(message);
    }




}
