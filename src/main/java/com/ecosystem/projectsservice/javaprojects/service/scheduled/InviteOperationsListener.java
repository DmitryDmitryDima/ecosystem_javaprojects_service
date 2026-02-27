package com.ecosystem.projectsservice.javaprojects.service.scheduled;

import com.ecosystem.projectsservice.javaprojects.model.ProjectInviteToken;
import com.ecosystem.projectsservice.javaprojects.repository.ProjectInviteTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/*
очистка used или expired токенов - приглашений в проект
 */
@Service
public class InviteOperationsListener {
    @Autowired
    private ProjectInviteTokenRepository inviteTokenRepository;

    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.SECONDS)
    public void clearTokens(){
        inviteTokenRepository.deleteAllExpiredTokens();
    }
}
