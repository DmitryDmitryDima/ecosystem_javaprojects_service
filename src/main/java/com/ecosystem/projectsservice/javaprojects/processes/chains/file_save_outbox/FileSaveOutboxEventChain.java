package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save_outbox;


import com.ecosystem.projectsservice.javaprojects.dto.RequestContext;
import com.ecosystem.projectsservice.javaprojects.dto.SecurityContext;
import com.ecosystem.projectsservice.javaprojects.processes.InternalEventsManager;
import com.ecosystem.projectsservice.javaprojects.processes.chains.ChainEvent;
import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save.FileSaveEventData;
import com.ecosystem.projectsservice.javaprojects.processes.chains.file_save.FileSaveInfo;
import com.ecosystem.projectsservice.javaprojects.processes.external_queue.ProjectEventContext;
import com.ecosystem.projectsservice.javaprojects.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class FileSaveOutboxEventChain {

    @Autowired
    private ObjectMapper objectMapper;

    // сюда мы пишем каждый следующий ивент
    @Autowired
    private OutboxEventRepository repository;





    // информация о цепочке
    @Autowired
    private InternalEventsManager eventsManager;

    // название результирующего ивента для внешней очереди
    private static final String externalQueueEventName = "java_project_file_save";

    // список ивентов, задействованных в очереди
    private List<Class<? extends ChainEvent>> events = List.of(FileSaveOutboxInitEvent.class);






    // цепочка регистрирует свои ивенты в сервисе. Сервис - мост между outbox обработчиком и цепочками.
    // нам достаточно вносить связь "название ивента - класс" при разработке цепочек
    @PostConstruct
    public void registerEvents(){
        eventsManager.registerChain(events);
    }






    // в методе инициации мы создаем макет данных для будущего результата всей цепочки. Помимо этого, мы производим первую публикацию в бд
    // если этот метод падает - пользователь получает мгновенный ответ с ошибкой
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void init(SecurityContext securityContext, RequestContext requestContext, FileSaveInfo info) throws Exception{
        // готовим transfer object - event data
        FileSaveEventData eventData = FileSaveEventData
                .builder()
                .fileId(info.getFileId())
                .content(info.getContent())
                .build();

        // готовим transfer object - event context
        ProjectEventContext context = ProjectEventContext.builder()
                .correlationId(requestContext.getCorrelationId())
                .renderId(requestContext.getRenderId())
                .timestamp(Instant.now())
                .userUUID(securityContext.getUuid())
                .username(securityContext.getUsername())
                .projectId(info.getProjectId())
                .build();

        FileSaveOutboxInitEvent initiationEvent = new FileSaveOutboxInitEvent(this, info.getProjectsPath());
        initiationEvent.setData(eventData);
        initiationEvent.setContext(context);

        // вставляем имя ивента откуда то...





    }



}
