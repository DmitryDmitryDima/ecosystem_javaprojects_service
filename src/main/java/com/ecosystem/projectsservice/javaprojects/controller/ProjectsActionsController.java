package com.ecosystem.projectsservice.javaprojects.controller;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.service.ProjectActionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/*
actions - общие действия с существующим проектом. Каждый запрос имеет id проекта


Анализируется security context - проверяется, допущен ли тот, кто просматривает проект, к проекту
Пропуски выдает хозяин проекта


Алгоритм на примере запроса к файлу:
- проверяем, существует ли проект по project_id
- проверяем, имеет ли доступ security_context.user_uuid к project_id
- проверяем, принадлежит ли file_id указанному проекту (одновременно - существует ли файл)

Исходя из этого, в базе данных нам не обязательно кешировать весь путь до файла или папки в файловой системе
мы всегда знаем uuid и project_name. Нам достаточно кешировать лишь внутреннюю структуру
Кеширование пути, таким образом, полезно лишь для внутренних функцию по типу формирования кеша запросов или ai анализа
При запросе нам все равно нужно проверять наличие файла, то есть проходить через структуру
 */

@RestController
@RequestMapping("/{id}/actions")
public class ProjectsActionsController {


    @Autowired
    private ProjectActionsService actionsService;




    // читаем проект, получаем всю необходимую информацию
    @GetMapping("/read")
    public ResponseEntity<ProjectDTO> read(@PathVariable("id") Long id, @RequestHeader Map<String, String> headers) throws Exception{

        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        return ResponseEntity.ok(actionsService.readProject(securityContext, requestContext, id));



    }


    // чтение файла - viewer id, project id, project author id, file id - все данные для конструирования пути

    //   @GetMapping("/readFile")















}
