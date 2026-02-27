package com.ecosystem.projectsservice.javaprojects.repository;

import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.ProjectInviteToken;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectInviteTokenRepository extends JpaRepository<ProjectInviteToken, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t from ProjectInviteToken t where t.id=?1")
    Optional<ProjectInviteToken> findByIdForUpdate(UUID id);

    @Transactional
    @Modifying
    @NativeQuery("delete from java_projects_invite_tokens t where t.used='true' or t.expired_at<now()")
    void deleteAllExpiredTokens();
}
