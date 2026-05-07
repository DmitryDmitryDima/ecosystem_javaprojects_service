package com.ecosystem.projectsservice.javaprojects.repository;

import com.ecosystem.projectsservice.javaprojects.model.ProjectParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectParticipantRepository extends JpaRepository<ProjectParticipant, UUID> {


    List<ProjectParticipant> findByUserUUID(UUID uuid);
}
