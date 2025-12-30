package com.ecosystem.projectsservice.javaprojects.processes;

import com.ecosystem.projectsservice.javaprojects.processes.chains.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.EventName;
import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox.FileSaveOutboxInitEvent;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.util.*;

@Service
public class InternalEventsManager {


    // список ивентов, задействованных в очереди
    private final List<Class<? extends ChainEvent>> unitedEventsBase = new ArrayList<>();


    public void registerChain(List< Class<? extends ChainEvent>> chainEvents){
        unitedEventsBase.addAll(chainEvents);
    }


    // анализируем аннотацию @EventName
    public void getEventByName(String name){

        unitedEventsBase.forEach(el->{
            EventName anno = el.getAnnotation(EventName.class);

            if (anno!=null){
                System.out.println(anno.value());
            }


        });


    }



}
