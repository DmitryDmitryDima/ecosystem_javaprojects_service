package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.publisher;


import com.ecosystem.projectsservice.javaprojects.model.OutboxEvent;
import com.ecosystem.projectsservice.javaprojects.repository.OutboxEventRepository;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.annotations.events.EventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.annotations.registry.ChainEventQualifier;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.OutputMetadata;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.ChainOutput;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

// отделяем логику записи в outbox составляющую

// TODO ДЛЯ PUBLISHER НЕОБХОДИМО СДЕЛАТЬ НЕ ТОЛЬКО ИНТЕРФЕЙС,
//  НО И ВНЕДРИТЬ ЗАВИСИМОСТЬ ОТ OUTBOX MODEL REPOSITORY, СДЕЛАВ CALLBACK ЧАСТЬЮ КОНТРАКТА
@Service
public class OutboxPublisher implements ChainPublisher {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ObjectMapper mapper;

    // метаданные
    @Override
    public void publish(ChainOutput output, OutputMetadata<?> metadata) {

        try {

            ChainEvent chainEvent = output.getEvent();

            String type
                    = chainEvent.getClass().getAnnotation(ChainEventQualifier.class).value();



            OutboxEvent event = OutboxEvent.builder()

                    .correlationId(chainEvent.getProcessId())
                    .status(output.getStatus())
                    .type(type)
                    .payload(mapper.writeValueAsString(chainEvent))
                    .last_update(output.getLast_update())
                    .expiredAt(null) // deprecated
                    .readExpiration(output.getReadExpiration())
                    .performanceExpirationPeriod(output.getPerformanceExpirationPeriod())
                    .build();

            transactionTemplate.execute(status -> {
                outboxEventRepository.save(event);

                return null;
            });
        }
        catch (Exception e){
            throw new PublisherException("ошибка публикации outbox сообщения: "+e.getMessage(),
                    "PUBLISHING_ERROR");
        }










    }



}
