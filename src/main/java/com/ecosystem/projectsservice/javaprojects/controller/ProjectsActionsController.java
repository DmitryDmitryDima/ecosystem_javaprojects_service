package com.ecosystem.projectsservice.javaprojects.controller;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.SimpleFileInfo;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.directories.DirectoryAddRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.directories.DirectoryRemovalRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files.FileAddRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files.FileMoveRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files.FileRemovalRequest;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.writing.files.FileSaveRequest;
import com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers.TriggerAnswer;
import com.ecosystem.projectsservice.javaprojects.service.projects.ProjectActionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;


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



    // эндпоинт для воздействия на триггер процесса внутри проекта
    // используется для ответов ui на polling ивенты
    @PostMapping("/trigger/{uuid}")
    public ResponseEntity<Void> triggerProcess(@PathVariable("uuid") UUID processId, @PathVariable("id") UUID projectId,
                                               @RequestHeader Map<String, String> headers, @RequestBody TriggerAnswer answer)
            throws Exception {

        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        actionsService.triggerPollingProcess(securityContext, requestContext, projectId, answer);

        return ResponseEntity.noContent().build();

    }


    // читаем проект, получаем всю необходимую информацию
    @GetMapping("/read")
    public ResponseEntity<ProjectDTO> read(@PathVariable("id") UUID id, @RequestHeader Map<String, String> headers) throws Exception{

        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        return ResponseEntity.ok(actionsService.readProject(securityContext, requestContext, id));



    }

    // обновляем отдельно список последних редактируемых файлов (файлы должны иметь статус visible)
    @GetMapping("/readRecentFiles")
    public ResponseEntity<List<SimpleFileInfo>> readRecentFiles(@PathVariable("id") UUID id, @RequestHeader Map<String, String> headers) throws Exception{
        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        return ResponseEntity.ok(actionsService.getRecentFiles(securityContext, requestContext, id));
    }


    // чтение файла - viewer id, project id, project author id, file id - все данные для конструирования пути

    @GetMapping("/readFile/{file_id}")
    public ResponseEntity<FileDTO> readFile(@PathVariable("id") UUID projectId, @PathVariable("file_id") Long fileId,
                                            @RequestHeader Map<String, String> headers) throws Exception{

        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        return ResponseEntity.ok(actionsService.readFile(securityContext, requestContext, projectId, fileId));
    }


    // удаление файла - операция предполагает мгновенную инвалидацию некоторых элементов в кеше проекта (предложки)
    @PostMapping("/removeFile")
    public ResponseEntity<Void> removeFile(@PathVariable("id") UUID projectId,
                                           @RequestBody FileRemovalRequest request,

                                           @RequestHeader Map<String, String> headers) throws Exception{

        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);
        actionsService.removeFile(securityContext, requestContext, projectId, request);

        return ResponseEntity.noContent().build();
    }


    /*
    сохранение файла через отдельную кнопку - гарантирует сохранение файла в диск (используется наравне с автосохранением в redis)

    todo для демонстрации оставляю метод извлечения cookie - пригодится при введении токена
     */
    @PostMapping("/saveFile")
    public ResponseEntity<Void> saveFile(@PathVariable("id") UUID projectId,
                                         @RequestHeader Map<String, String> headers, @RequestBody FileSaveRequest request,

                                         @CookieValue(required = false, name = "accessToken") String accessToken

                                         ) throws Exception{


        System.out.println(accessToken);
        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        System.out.println(Arrays.toString(request.getContent().split("\\n")));

        actionsService.saveFile(securityContext, requestContext, projectId, request);

        return ResponseEntity.noContent().build();

    }

    /*
    автосохранение - будет происходить через редис
     */

    @PostMapping ("/autosave")
    public ResponseEntity<Void> autosave(@PathVariable("id") UUID projectId,
                                         @RequestHeader Map<String, String> headers, @RequestBody FileSaveRequest request) throws Exception{


        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        actionsService.autosave(securityContext, requestContext, projectId, request);


        return ResponseEntity.noContent().build();
    }


    @PostMapping("/addFile")
    public ResponseEntity<Void> addFile(@PathVariable("id") UUID projectId,
                                        @RequestHeader Map<String, String> headers, @RequestBody FileAddRequest request) throws Exception{

        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        actionsService.addFile(securityContext, requestContext, projectId, request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/moveFile")
    public ResponseEntity<Void> moveFile(@PathVariable("id") UUID projectId,
                                         @RequestHeader Map<String, String> headers, @RequestBody FileMoveRequest fileMoveRequest) throws Exception {
        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        actionsService.moveFile(securityContext, requestContext, projectId, fileMoveRequest);



        return ResponseEntity.noContent().build();
    }

    @PostMapping("/addDirectory")
    public ResponseEntity<Void> addDirectory(@PathVariable("id") UUID projectId,
                                             @RequestHeader Map<String, String> headers, @RequestBody DirectoryAddRequest request) throws Exception{
        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        actionsService.addDirectory(securityContext, requestContext, projectId, request);

        return ResponseEntity.noContent().build();



    }

    @PostMapping("/removeDirectory")
    public ResponseEntity<Void> removeDirectory(@PathVariable("id") UUID projectId,
                                                @RequestHeader Map<String, String> headers, @RequestBody DirectoryRemovalRequest request)

    throws Exception
    {

        SecurityContext securityContext = SecurityContext.generateContext(headers);
        RequestContext requestContext = RequestContext.generateRequestContext(headers);

        actionsService.removeDirectory(securityContext, requestContext, projectId, request);


        return ResponseEntity.noContent().build();


    }




















}
