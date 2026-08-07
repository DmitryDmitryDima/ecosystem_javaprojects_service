package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.control;

import org.springframework.scheduling.annotation.Scheduled;

public class ProcessAvatarStorageSpringScheduledVersion extends ProcessAvatarStorageImpl {



    // отдаем управление spring
    @Scheduled(fixedDelayString = "${avatar.storage.clearing:10000}")
    @Override
    public void clearTerminatedAvatars() {
        super.clearTerminatedAvatars();
    }
}
