package com.ecosystem.projectsservice.javaprojects.controller;


import com.ecosystem.projectsservice.javaprojects.dto.dashboard.AvatarDTO;
import com.ecosystem.projectsservice.javaprojects.dto.dashboard.IndexGroupDTO;
import com.ecosystem.projectsservice.javaprojects.service.dashboard.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


// контроллер, обслуживающий различные фронтенд инструменты тестирования процессов


@RestController
@RequestMapping("/dashboard")
public class DashboardController {


    @Autowired
    private DashboardService dashboardService;



    @GetMapping("/avatars")
    public ResponseEntity<List<AvatarDTO>> getAvatars(){


        return ResponseEntity.ok(dashboardService.getAllAvatars());
    }


    @GetMapping("/indexes")
    public ResponseEntity<List<IndexGroupDTO>> getIndexes(){

        return ResponseEntity.ok(dashboardService.getAllIndexGroups());
    }


    @PostMapping("/click")
    public ResponseEntity<Void> runButtonAction(){

        dashboardService.runTestButton();


        return ResponseEntity.noContent().build();
    }



}
