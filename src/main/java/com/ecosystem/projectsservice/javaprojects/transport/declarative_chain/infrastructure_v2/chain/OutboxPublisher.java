package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain;


import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.events.ChainEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

// отделяем логику записи в outbox составляющую
@Service
public class OutboxPublisher implements ChainPublisher {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ObjectMapper mapper;

    // метаданные
    @Override
    public void publish(ChainOutput output, OutputMetadata<?> metadata) {

        ChainEvent chainEvent = output.getEvent();






    }



}
