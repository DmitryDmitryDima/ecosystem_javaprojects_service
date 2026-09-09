package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.control;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.trigger.storage.DefaultTriggerRuntimeStorage;
import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox.OutboxModelRepository;
import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.annotation.Scheduled;

public class TriggerRuntimeStorageSpring extends DefaultTriggerRuntimeStorage {


    public TriggerRuntimeStorageSpring(OutboxModelRepository repository) {
        super(repository);
    }

    @Override
    @Scheduled(fixedDelayString = "${trigger.storage.clearing:10000}")
    public void clear() {
        super.clear();
    }


    @PreDestroy
    @Override
    public void destroy() {
        super.destroy();
    }
}
