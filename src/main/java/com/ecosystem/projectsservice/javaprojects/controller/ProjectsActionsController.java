package com.ecosystem.projectsservice.javaprojects.controller;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.SimpleFileInfo;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.FileSaveRequest;
import com.ecosystem.projectsservice.javaprojects.service.ProjectActionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    // обновляем отдельно список последних редактируемых файлов (файлы должны иметь статус visible)
    @GetMapping("/readRecentFiles")
    public ResponseEntity<List<SimpleFileInfo>> readRecentFiles(@PathVariable("id") Long id, @RequestHeader Map<String, String> headers) throws Exception{
        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        return ResponseEntity.ok(actionsService.getRecentFiles(securityContext, requestContext, id));
    }


    // чтение файла - viewer id, project id, project author id, file id - все данные для конструирования пути

    @GetMapping("/readFile/{file_id}")
    public ResponseEntity<FileDTO> readFile(@PathVariable("id") Long projectId, @PathVariable("file_id") Long fileId,
                                            @RequestHeader Map<String, String> headers) throws Exception{

        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        return ResponseEntity.ok(actionsService.readFile(securityContext, requestContext, projectId, fileId));
    }

    /*
    сохранение файла через отдельную кнопку - гарантирует сохранение файла в диск (используется наравне с автосохранением в redis)
     */
    @PostMapping("/saveFile/{file_id}")
    public ResponseEntity<Void> saveFile(@PathVariable("id") Long projectId, @PathVariable("file_id") Long fileId,
                                         @RequestHeader Map<String, String> headers, @RequestBody FileSaveRequest request) throws Exception{

        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        actionsService.saveFile(securityContext, requestContext, projectId, fileId, request);

        return ResponseEntity.noContent().build();

    }

    /*
    автосохранение - происходит в redis
     */

    @PostMapping ("/autosave/{file_id}")
    public ResponseEntity<Void> autosave(@PathVariable("id") Long projectId, @PathVariable("file_id") Long fileId,
                                         @RequestHeader Map<String, String> headers, @RequestBody FileSaveRequest request) throws Exception{


        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);


        return ResponseEntity.noContent().build();
    }




















}
