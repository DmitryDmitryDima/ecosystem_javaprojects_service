package com.ecosystem.projectsservice.javaprojects.message_queue;

import com.ecosystem.projectsservice.javaprojects.message_queue.events_for_queue.ProjectRemovalResultEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// слушаются внутренние события микросервиса, в зависимости от
@Component
public class InternalEventsListener {


    @EventListener
    public void processProjectRemovalResult(ProjectRemovalResultEvent event){

        System.out.println(event+" will go to rabbitmq");
    }


}
