package com.ecosystem.projectsservice.javaprojects.external_messaging.broadcast;


import com.ecosystem.projectsservice.javaprojects.transport.broadcast.Broadcast;
import com.ecosystem.projectsservice.javaprojects.transport.broadcast.BroadcastException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MessageBroadcast {




    @Autowired
    private ApplicationEventPublisher publisher;




    @Async("virtualThreadFactory")
    public void sendAsync()  {

    }


    public void sendSync(){


    }









}
