package com.ecosystem.projectsservice.javaprojects.service.state;

import com.ecosystem.projectsservice.javaprojects.dto.projects.state.Autosave;
import com.ecosystem.projectsservice.javaprojects.transport.broadcast.Broadcast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// данный класс предоставляет хуки, вызываемые при совершении каких либо действий с кодовой базой
@Service
public class StateListener {


    @Autowired
    private Broadcast broadcast;






    public void onAutosave(Autosave autosave){



    }


}
