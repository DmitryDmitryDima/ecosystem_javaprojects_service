package com.ecosystem.projectsservice.javaprojects.controller;


import com.ecosystem.projectsservice.javaprojects.dto.dashboard.AvatarDTO;
import com.ecosystem.projectsservice.javaprojects.service.dashboard.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
