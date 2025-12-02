package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.processes.queue.UserEventContext;
import com.ecosystem.projectsservice.javaprojects.processes.chains.project_removal.ProjectRemovalStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.UUID;

@SpringBootTest
public class MessageQueueTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${users.activity_events.exchange.name}")
    private String USERS_ACTIVITY_EXCHANGE_NAME;




}
