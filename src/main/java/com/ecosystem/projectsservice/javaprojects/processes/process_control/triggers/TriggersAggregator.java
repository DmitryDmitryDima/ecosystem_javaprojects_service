package com.ecosystem.projectsservice.javaprojects.processes.process_control.triggers;

import com.ecosystem.projectsservice.javaprojects.processes.external_events.EventStatus;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.context.ProjectEventFromSystemContext;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.data.triggers.SimpleUserControlledProjectTriggerData;
import com.ecosystem.projectsservice.javaprojects.processes.external_events.event_categories.ProjectEventFromSystem;
import com.ecosystem.projectsservice.javaprojects.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


// данная система работает в совокупности с механизмом waiting for
// юзер, проектируя цепочку, должен создать триггер, который будет активировать какими-либо действиями из ui.
// Активация триггера позволяет перевести ожидающий Outbox event из состояния waiting_for_external в waiting
// при устаревании outbox автоматически запускается компенсационное событие, сигнализирующее, что условие для запуска следующего шага не выполнено
@Service
public class TriggersAggregator {





    @Autowired
    private ObjectMapper mapper;

    private ConcurrentHashMap<UUID, Trigger> triggers = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private OutboxEventRepository outboxEventRepository;




    public void registerTrigger(Trigger trigger){
        triggers.put(trigger.getParentProcess(), trigger);
    }

    // ui определяет, причастен ли он к триггеру, если да - занимает место
    public void takeASeat(UUID userUUID, UUID correlationId){
        Trigger trigger = triggers.get(correlationId);
        // если активность собрана (таймер), то регистрация прекращается
        if (trigger == null || trigger.getActivityGained().get()) return;

        if (trigger instanceof SimpleUserControlledProjectTrigger simpleUserControlledProjectTrigger){
            ConcurrentHashMap<UUID, Boolean> opinions = simpleUserControlledProjectTrigger.getOpinions();
            opinions.put(userUUID, false);

        }
    }


    // фаза инициации - сбор информации о юзерах с помощью специального ивента
    public void initiateTrigger(UUID correlationId){

        Trigger trigger = triggers.get(correlationId);

        if (trigger==null){
            return;
        }

        if (trigger instanceof SimpleUserControlledProjectTrigger simpleProjectTrigger){




            ProjectEventFromSystem externalEvent = new ProjectEventFromSystem();
            externalEvent.setStatus(EventStatus.ACTIVITY_POLL);
            externalEvent.setType(simpleProjectTrigger.getTriggerType().getValue());
            ProjectEventFromSystemContext context = ProjectEventFromSystemContext
                    .builder()
                    .projectId(simpleProjectTrigger.getProjectId())
                    .origin("trigger activity phase")
                    .build();

            SimpleUserControlledProjectTriggerData data = new SimpleUserControlledProjectTriggerData();
            data.setFileId(simpleProjectTrigger.getFileId());

            externalEvent.setMessage("Request activity");
            externalEvent.setContext(context);
            try {
                externalEvent.setData(mapper.writeValueAsString(data));
                publisher.publishEvent(externalEvent);

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }


        }
    }

    @Scheduled(fixedRate = 1000*60*60)
    public void clearExpiredTriggers(){
        Instant now = Instant.now();
        triggers.entrySet().removeIf(entry-> entry.getValue().getExpiredAt().isBefore(now));
    }


}
