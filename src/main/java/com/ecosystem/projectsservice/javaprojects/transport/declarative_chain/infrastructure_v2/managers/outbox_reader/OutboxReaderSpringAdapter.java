package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.outbox_reader;

import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager.EventManager;
import com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.OutboxModelRepository;
import org.springframework.scheduling.annotation.Scheduled;

public class OutboxReaderSpringAdapter extends OutboxReaderDefault
{


    public OutboxReaderSpringAdapter(OutboxModelRepository repository,
                                     EventManager manager) {
        super(repository, manager);
    }

    @Override
    @Scheduled(fixedDelayString = "${reader.waiting.events:500}")
    public void readWaitingEvents() {
        super.readWaitingEvents();
    }

    @Override
    @Scheduled(fixedDelayString = "${reader.waiting.events.expired:20000}")
    public void readExpiredWaitingEvents() {
        super.readExpiredWaitingEvents();
    }

    @Override
    @Scheduled(fixedDelayString = "${reader.processing.events.expired:20000}")
    public void readExpiredProcessingEvents() {
        super.readExpiredProcessingEvents();
    }



    @Override
    @Scheduled(fixedDelayString = "${reader.processing.events.everlasting:20000}")
    public void readEverlastingProcessingEvents() {
        super.readEverlastingProcessingEvents();
    }

    @Override
    @Scheduled(fixedDelayString = "${reader.processing.events.missed:60000}")
    public void readMissedExpiredProcessingEvents() {
        super.readMissedExpiredProcessingEvents();
    }

    @Override
    @Scheduled(fixedDelayString = "${reader.manager.crashed.events:60000}")
    public void readManagerCrashedEvents() {
        super.readManagerCrashedEvents();
    }

    @Override
    @Scheduled(fixedDelayString = "${reader.waiting.for.signal.events:2000}")
    public void readExpiredWaitingForSignalEvents() {
        super.readExpiredWaitingForSignalEvents();
    }
}
