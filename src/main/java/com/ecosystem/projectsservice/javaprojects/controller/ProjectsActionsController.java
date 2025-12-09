package com.ecosystem.projectsservice.javaprojects.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/*
actions - общие действия с существующим проектом


Анализируется security context - проверяется, допущен ли тот, кто просматривает проект, к проекту
Пропуски выдает хозяин проекта
 */

@RestController
@RequestMapping("/{id}/actions")
public class ProjectsActionsController {













}
