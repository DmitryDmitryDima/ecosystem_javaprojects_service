package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework_spring.control;

import com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.control.avatar.ProcessAvatarStorageImpl;
import org.springframework.scheduling.annotation.Scheduled;

public class ProcessAvatarStorageSpringScheduledVersion extends ProcessAvatarStorageImpl {



    // отдаем управление spring
    @Scheduled(fixedDelayString = "${avatar.storage.clearing:10000}")
    @Override
    public void clearTerminatedAvatars() {
        super.clearTerminatedAvatars();
    }
}
