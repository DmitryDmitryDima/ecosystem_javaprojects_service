package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.outbox_reader;




public class OutboxReaderSpringAdapter extends OutboxReaderDefault {


    // todo сделать scheduled с возможностью настройки времени из конфигурации

    @Override
    public void readProcessingEvents() {
        super.readProcessingEvents();
    }

    @Override
    public void readWaitingEvents() {
        super.readWaitingEvents();
    }

    @Override
    public void readWaitingForSignalEvents() {
        super.readWaitingForSignalEvents();
    }
}
